import { useParams } from 'react-router-dom';
import QuizCreateEditForm from '../components/QuizCreateEditForm';
import { useEffect, useRef, useState } from 'react';
import { AnswerResultStat, ModeratorState, Quiz } from '../types/quiz';
import { Button, Spin, Typography } from 'antd';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import QuestionResultStatCard from '../components/QuestionResultStatCard';
import QuizDetailsCard from '../components/QuizDetailsCard';
import Title from 'antd/es/typography/Title';

const { Text } = Typography;

function QuizDetailsPage() {
  const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;
  const { quizId } = useParams();
  const [quiz, setQuiz] = useState<Quiz | undefined>(undefined);
  const [editMode, setEditMode] = useState(false);
  const [quizCode, setQuizCode] = useState<string | null>(null);
  const [participantCount, setParticipantCount] = useState<number>(0);
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [showResults, setShowResults] = useState(false);
  const [resultsStat, setResultsStat] = useState<AnswerResultStat[] | null>(null);

  const stompClientRef = useRef<Client | null>(null);

  const handleStateMessage = (message: any) => {
    const state: ModeratorState = JSON.parse(message.body);
    if (state.participantCount != participantCount) {
      setParticipantCount(state.participantCount);
    }
    if (state.currentQuestionIndex != currentQuestionIndex) {
      setCurrentQuestionIndex(state.currentQuestionIndex);
    }
    setResultsStat(state.resultsStat); // AnswerResultStat[]
    if (state.showResults != showResults) {
      setShowResults(state.showResults);
    }
  };

  // Dohvat kviza
  useEffect(() => {
    const fetchQuiz = async () => {
      const response = await fetch(`${API_BASE_URL}/quiz/${quizId}`);
      if (!response.ok) return console.error('Failed to load quiz');
      const data = await response.json();
      setQuiz(data);
    };

    if (quizId) {
      fetchQuiz();
    }
  }, [quizId]);

  const connectToWebSocket = (isRefresh: boolean) => {
    const storedCode = localStorage.getItem('quizCode');
    if (isRefresh && !storedCode) {
      // ako se dogodio refresh, a nema pohranjenog koda, ne spajaj se
      return;
    }
    // inače se spoji ili sa pohranjenim kodom ako postoji ili kreiraj kod
    const socket = new SockJS(`${API_BASE_URL}/stomp-endpoint`);
    const client = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000,
      onConnect: async () => {
        console.log('Moderator connected to WebSocket');

        try {
          let code;

          if (storedCode) {
            setQuizCode(storedCode);
            code = storedCode;
          } else {
            const response = await fetch(`${API_BASE_URL}/quiz-instance/${quizId}/start`, {
              method: 'POST',
            });
            const data = await response.json();
            setQuizCode(data.quizCode);
            localStorage.setItem('quizCode', data.quizCode);
            code = data.quizCode;
          }

          // Pretplata na stanje
          client.subscribe(`/topic/quiz/${code}/moderatorState`, handleStateMessage);

          // Dohvat stanja
          client.publish({
            destination: `/app/quiz/${code}/getModeratorState`, // trigera moderatorState
            body: JSON.stringify({}),
          });
        } catch (error) {
          console.error('Failed to start quiz instance:', error);
        }
      },
      onStompError: frame => {
        console.error('STOMP protocol error:', frame.headers.message);
      },
    });

    client.activate();
    stompClientRef.current = client;

    return () => {
      if (client.connected) {
        client.deactivate();
      }
    };
  };

  useEffect(() => {
    // ponovno spajanje na WebSocket ako se dogodi refresh stranice
    connectToWebSocket(true);
  }, []);

  const startQuestion = () => {
    if (!stompClientRef.current?.connected || !quizCode || !quiz) {
      return;
    }

    const question = quiz.questions[currentQuestionIndex];
    if (!question) {
      return;
    }

    // Objava novog pitanja
    stompClientRef.current.publish({
      destination: `/app/quiz/${quizCode}/question`,
      body: JSON.stringify(question),
    });

    setCurrentQuestionIndex(prev => prev + 1);
    setShowResults(false);
    setResultsStat([]);
  };

  const showVotingResults = () => {
    if (!stompClientRef.current?.connected || !quizCode || !quiz) return;
    const question = quiz.questions[currentQuestionIndex - 1];
    if (!question) return;

    // Objava da se rezultati trebaju prikazati
    stompClientRef.current.publish({
      destination: `/app/quiz/${quizCode}/showResults`,
      body: JSON.stringify({}),
    });

    setShowResults(true);
  };

  const handleQuizUpdated = (updatedQuiz: Quiz) => {
    setQuiz(updatedQuiz);
    setEditMode(false);
  };

  const disconnectFromWebSocket = async () => {
    if (!quizCode) return;

    // Signal da se participants ugase
    if (stompClientRef.current?.connected) {
      stompClientRef.current.publish({
        destination: `/app/quiz/${quizCode}/end`,
        body: JSON.stringify({}),
      });
    }

    try {
      // Označi instancu kviza neaktivnom
      const response = await fetch(`${API_BASE_URL}/quiz-instance/${quizCode}/end`, {
        method: 'PUT',
      });

      if (!response.ok) throw new Error('Failed to end quiz instance.');

      setQuizCode(null);
      setParticipantCount(0);
      localStorage.removeItem('quizCode');
      setCurrentQuestionIndex(0);
      setResultsStat([]);

      if (stompClientRef.current) {
        stompClientRef.current.deactivate();
        console.log('WebSocket disconnected.');
      }
    } catch (error) {
      console.error('Error ending quiz instance:', error);
    }
  };

  if (!quiz) return <Spin></Spin>;

  return (
    <div style={{ padding: '2rem' }}>
      <div style={{ display: 'flex', justifyContent: 'center', columnGap: 20 }}>
        {quizCode ? (
          <Button type="primary" danger={true} onClick={disconnectFromWebSocket}>
            Stop Quiz
          </Button>
        ) : (
          <Button
            type="primary"
            onClick={() => connectToWebSocket(false)}
            disabled={!quiz?.questions || quiz.questions.length === 0}
          >
            Start Quiz
          </Button>
        )}
        {quizCode && (
          <>
            {currentQuestionIndex > 0 &&
              !showResults && ( // ako je false gumb se prikazuje i postavlja na true
                <Button type="primary" onClick={showVotingResults}>
                  Show Results {currentQuestionIndex}
                </Button>
              )}

            {(showResults || currentQuestionIndex === 0) &&
              currentQuestionIndex < quiz.questions.length && ( // ako je true, gumb se prikazuje i postavlja na false
                <Button
                  type="primary"
                  onClick={startQuestion}
                  //disabled={currentQuestionIndex >= quiz.questions.length}
                >
                  Start Question {currentQuestionIndex + 1}
                </Button>
              )}
          </>
        )}
        <Button type="primary" danger={editMode} onClick={() => setEditMode(prev => !prev)}>
          {editMode ? 'Cancel' : 'Edit'}
        </Button>
      </div>
      {!editMode && quizCode && (
        <div style={{ display: 'flex', flexDirection: 'column', margin: 20, marginTop: 40 }}>
          <Title level={4}>
            Quiz Code:{' '}
            <Text copyable style={{ font: 'inherit' }}>
              {quizCode}
            </Text>
          </Title>
          <Title level={4}>Participants: {participantCount}</Title>
        </div>
      )}

      {stompClientRef.current?.connected &&
        quizCode &&
        quiz &&
        currentQuestionIndex > 0 && ( // live statistika glasanja
          <QuestionResultStatCard
            quizName={quiz.name}
            questionText={quiz.questions[currentQuestionIndex - 1]?.text}
            questionAnswerOptions={quiz.questions[currentQuestionIndex - 1]?.answerOptions || []}
            resultsStat={resultsStat}
          />
        )}

      {editMode ? (
        <QuizCreateEditForm existingQuiz={quiz} onQuizCreated={handleQuizUpdated} />
      ) : (
        <QuizDetailsCard quiz={quiz} />
      )}
    </div>
  );
}

export default QuizDetailsPage;
