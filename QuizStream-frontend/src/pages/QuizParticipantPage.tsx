import { useEffect, useRef, useState } from 'react';
import { AnswerResultStat, ParticipantAnswers, Question } from '../types/quiz';
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
  const [participantAnswer, setAnswer] = useState<ParticipantAnswers | null>(null);
  const [isValidQuiz, setIsValidQuiz] = useState<boolean | null>(null);
  const [hasAnswered, setHasAnswered] = useState(false);
  const [resultsStat, setResultsStat] = useState<AnswerResultStat[]>([]);
  const [showStat, setShowStat] = useState(false);

  const stompClientRef = useRef<Client | null>(null);

  // čišćenje pohrane prethodnih kvizova
  useEffect(() => {
    if (!quizCode) return;
    Object.keys(localStorage).forEach(key => {
      if (key.startsWith('quiz_') && !key.includes(quizCode)) {
        localStorage.removeItem(key);
      }
    });
  }, [quizCode]);

  useEffect(() => {
    if (!quizCode || !question) return;

    // Očisti prethodne odgovore
    setAnswer(null);

    // Provjeri je li sudionik već glasao za trenutno pitanje
    const hasVotedKey = `quiz_${quizCode}_question_${question.id}`;
    const hasVoted = localStorage.getItem(hasVotedKey) === 'true';

    if (hasVoted) {
      setHasAnswered(true);

      // Zatraži rezultate ako su dostupni
      if (stompClientRef.current?.connected) {
        stompClientRef.current.publish({
          destination: `/app/quiz/${quizCode}/getResults`,
          body: question.id,
        });
      }
    }
  }, [question, quizCode]);

  useEffect(() => {
    if (!quizCode) return;

    // Provjeri valjanost kviza prije spajanja
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

  useEffect(() => {
    if (!quizCode || !isValidQuiz || stompClientRef.current?.connected) return;

    const socket = new SockJS(`${API_BASE_URL}/stomp-endpoint`);
    const client = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000,
      onConnect: () => {
        console.log('Connected to WebSocket');

        // Pretplata na trenutno pitanje
        client.subscribe(`/topic/quiz/${quizCode}/question`, msg => {
          const newQuestion: Question = JSON.parse(msg.body);
          setQuestion(newQuestion);
          setHasAnswered(false); // resetira status za novo pitanje
          setResultsStat([]);
          setShowStat(false);
        });

        // Pretplata na rezultate
        client.subscribe(`/topic/quiz/${quizCode}/finalResults`, message => {
          const parsedResults: AnswerResultStat[] = JSON.parse(message.body);
          setResultsStat(parsedResults);
          setShowStat(true);
        });

        // Pretplata na kraj kviza
        client.subscribe(`/topic/quiz/${quizCode}/end`, _msg => {
          if (stompClientRef.current) {
            stompClientRef.current.deactivate();
            console.log('WebSocket disconnected.');
          }
          setQuestion(null);
          setAnswer(null);
          setIsValidQuiz(null);
          setHasAnswered(false);
          navigate('/');
        });

        // Pretplata na trenutno stanje
        client.subscribe(`/topic/quiz/${quizCode}/state`, msg => {
          const state = JSON.parse(msg.body);
          if (state.currentQuestion) {
            setQuestion(state.currentQuestion);
          }
          if (state.showResults) {
            // Zatraži rezultate ako su dostupni
            client.publish({
              destination: `/app/quiz/${quizCode}/getResults`,
              body: state.currentQuestion?.id || '',
            });
          }
        });

        // Zatraži trenutno stanje
        client.publish({
          destination: `/app/quiz/${quizCode}/getState`, // trigera /state
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
      setAnswer({
        questionId: question.id,
        answerIds: [answerId],
      });
    } else {
      // Ako postoji već za isto pitanje
      const alreadySelected = participantAnswer.answerIds.includes(answerId);
      setAnswer({
        ...participantAnswer,
        answerIds: alreadySelected
          ? participantAnswer.answerIds.filter(id => id !== answerId)
          : [...participantAnswer.answerIds, answerId],
      });
    }
  };

  const sendAnswer = () => {
    if (!participantAnswer || !stompClientRef.current?.connected) return;

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

  if (isValidQuiz === false) {
    return null; // Već smo preusmjerili na home page
  }

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
