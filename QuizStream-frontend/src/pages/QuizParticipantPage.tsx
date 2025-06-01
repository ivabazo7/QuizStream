import { useEffect, useRef, useState } from 'react';
import { AnswerResultStat, ParticipantAnswers, ParticipantState, Question } from '../types/quiz';
import { Button, message, Spin } from 'antd';
import { useNavigate, useParams } from 'react-router-dom';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import QuestionResultStatCard from '../components/QuestionResultStatCard';
import QuestionAnswerCard from '../components/QuestionAnswerCard';

function QuizParticipantPage() {
  const { quizCode } = useParams();
  const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;
  const navigate = useNavigate();

  const [question, setQuestion] = useState<Question | null>(null);
  const [participantAnswer, setParticipantAnswer] = useState<ParticipantAnswers | null>(null);
  const [isValidQuiz, setIsValidQuiz] = useState<boolean | null>(null);
  const [hasAnswered, setHasAnswered] = useState(false);
  const [resultsStat, setResultsStat] = useState<AnswerResultStat[] | null>(null);
  const [showStat, setShowStat] = useState(false);

  const stompClientRef = useRef<Client | null>(null);

  const handleStateMessage = (msg: any) => {
    const state: ParticipantState = JSON.parse(msg.body);

    if (state.currentQuestion !== null) {
      setQuestion(state.currentQuestion);
      const hasVotedKey = `quiz_${quizCode}_question_${state.currentQuestion.id}`;
      const hasVoted = localStorage.getItem(hasVotedKey) === 'true';
      if (hasVoted) {
        setHasAnswered(true);
        setParticipantAnswer(null);
      } else {
        localStorage.setItem(hasVotedKey, 'false');
        setHasAnswered(false);
      }
    }
    setShowStat(state.showResults);
    setResultsStat(state.resultsStat);
  };

  // Čišćenje pohrane prethodnih kvizova
  useEffect(() => {
    if (!quizCode) return;
    Object.keys(localStorage).forEach(key => {
      if (key.startsWith('quiz_') && !key.includes(quizCode)) {
        localStorage.removeItem(key);
      }
    });
  }, [quizCode]);

  // Provjera valjanosti koda kviza prije spajanja
  useEffect(() => {
    if (!quizCode) return;

    const validateQuizCode = async () => {
      try {
        const response = await fetch(`${API_BASE_URL}/quiz-instance/validate-code/${quizCode}`);
        if (!response.ok) throw new Error('Validation failed');

        const isValid = await response.json();
        setIsValidQuiz(isValid);

        if (!isValid) {
          message.error('Invalid or expired quiz code!');
          navigate('/');
          return;
        }
      } catch (error) {
        console.error('Error validating quiz code:', error);
        message.error('Failed to validate quiz code. Please try again.');
        navigate('/');
      }
    };

    validateQuizCode();
  }, [quizCode]);

  // Spajanje na WebSocket koristeći valjani kod kviza
  useEffect(() => {
    if (!quizCode || !isValidQuiz || stompClientRef.current?.connected) return;

    const socket = new SockJS(`${API_BASE_URL}/stomp-endpoint`);
    const client = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000,
      onConnect: () => {
        console.log('Connected to WebSocket');

        // Pretplata na kraj kviza
        client.subscribe(`/topic/quiz/${quizCode}/end`, _msg => {
          if (stompClientRef.current) {
            stompClientRef.current.deactivate();
            console.log('WebSocket disconnected.');
          }
          setQuestion(null);
          setParticipantAnswer(null);
          setIsValidQuiz(null);
          setHasAnswered(false);
          navigate('/');
        });

        // Pretplata na trenutno stanje
        client.subscribe(`/topic/quiz/${quizCode}/participantState`, handleStateMessage);

        // Dohvat trenutnog stanja
        client.publish({
          destination: `/app/quiz/${quizCode}/getParticipantState`, // trigera state
          body: JSON.stringify({}),
        });
      },
      onDisconnect: () => {
        console.log('Disconnected from WebSocket');
      },
      onStompError: frame => {
        console.error('STOMP error:', frame.headers.message);
        message.error('Connection error. Please try again.');
        navigate('/');
      },
    });

    client.activate();
    stompClientRef.current = client;

    return () => {
      client.deactivate();
      console.log('WebSocket client deactivated');
    };
  }, [quizCode, isValidQuiz]);

  const handleAnswerClick = (answerId: string) => {
    if (!question) return;

    if (!participantAnswer || participantAnswer.questionId !== question.id) {
      // Ako nema odgovora ili je za drugo pitanje
      setParticipantAnswer({
        questionId: question.id,
        answerIds: [answerId],
      });
    } else {
      // Ako postoji već za isto pitanje
      const alreadySelected = participantAnswer.answerIds.includes(answerId);
      setParticipantAnswer({
        ...participantAnswer,
        answerIds: alreadySelected
          ? participantAnswer.answerIds.filter(id => id !== answerId)
          : [...participantAnswer.answerIds, answerId],
      });
    }
  };

  const sendAnswer = () => {
    if (!participantAnswer || !stompClientRef.current?.connected) return;

    // Slanje odgovora
    stompClientRef.current.publish({
      destination: `/app/quiz/${quizCode}/answer`,
      body: JSON.stringify(participantAnswer),
    });

    // Spremi stanje glasanja u lokalnu pohranu
    const hasVotedKey = `quiz_${quizCode}_question_${question?.id}`;
    localStorage.setItem(hasVotedKey, 'true');

    setHasAnswered(true); // Spriječi ponovni odgovor
    message.success('Hvala na odgovoru! Čekajte sljedeće pitanje...');
  };

  return (
    <>
      {!question ? (
        <div style={{ marginTop: '2rem', fontSize: '1.2rem' }}>
          Čekajte da moderator započne pitanje... <Spin />
        </div>
      ) : hasAnswered || showStat ? (
        <div style={{ marginTop: '2rem', fontSize: '1.2rem' }}>
          Hvala na odgovoru! Čekajte rezultate i sljedeće pitanje...
          {stompClientRef.current?.connected && quizCode && (
            <QuestionResultStatCard
              questionText={question.text}
              questionAnswerOptions={question.answerOptions}
              resultsStat={resultsStat}
            />
          )}
        </div>
      ) : (
        <>
          <QuestionAnswerCard
            question={question}
            participantAnswer={participantAnswer}
            handleAnswerClick={handleAnswerClick}
          />
          <Button type="primary" onClick={sendAnswer} disabled={!participantAnswer}>
            Pošalji Odgovor
          </Button>
        </>
      )}
    </>
  );
}

export default QuizParticipantPage;
