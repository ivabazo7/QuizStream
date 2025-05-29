import { useParams } from 'react-router-dom';
import QuizCreateEditForm from '../components/QuizCreateEditForm';
import { useEffect, useRef, useState } from 'react';
import { AnswerResultStat, Quiz } from '../types/quiz';
import { Button, Typography } from 'antd';
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
  const [resultsStat, setResultsStat] = useState<AnswerResultStat[]>([]);

  const stompClientRef = useRef<Client | null>(null);

  const getTempResults = (questionIdx: string) => {
    if (stompClientRef.current?.connected && quizCode) {
      // Zatraži rezultate za trenutno pitanje
      stompClientRef.current.publish({
        destination: `/app/quiz/${quizCode}/getTempResults`,
        body: questionIdx,
      });
    }
  };

  const handleStateMessage = (message: any) => {
    const state = JSON.parse(message.body);
    setParticipantCount(state.participantCount);
    if (state.currentQuestionIndex > currentQuestionIndex) {
      setCurrentQuestionIndex(state.currentQuestionIndex);
    } else {
    }
    setResultsStat(state.resultsStat || []);
    setShowResults(state.showResults || false);
  };

  const handleShowResultsMessage = (message: any) => {
    console.log('/showResults', message);
    const parsedResults: AnswerResultStat[] = JSON.parse(message.body);
    console.log(parsedResults);
    setResultsStat(parsedResults);
  };

  const handleParticipantsMessage = (message: any) => {
    const count = parseInt(message.body);
    console.log(`Received participant count update: ${count}`);
    setParticipantCount(count);
  };

  const handleFinalResultsMessage = (message: any) => {
    const results: AnswerResultStat[] = JSON.parse(message.body);
    setResultsStat(results);
  };

  // Prikupi stanje nakon spajanja
  useEffect(() => {
    if (!stompClientRef.current?.connected || !quizCode) return;

    const stateSub = stompClientRef.current.subscribe(
      `/topic/quiz/${quizCode}/state`,
      handleStateMessage
    );

    // Zatraži trenutno stanje
    stompClientRef.current.publish({
      destination: `/app/quiz/${quizCode}/getState`, // trigera state
      body: JSON.stringify({}),
    });

    return () => {
      if (stompClientRef.current?.connected) {
        stateSub.unsubscribe();
      }
    };
  }, [quizCode]);

  // Prikazi rezultate ako su dostupni
  useEffect(() => {
    if (!stompClientRef.current?.connected || !quizCode || !quiz || !showResults) return;

    const question = quiz.questions[currentQuestionIndex - 1];
    if (!question) return;

    const finalResultsSub = stompClientRef.current.subscribe(
      `/topic/quiz/${quizCode}/finalResults`,
      handleFinalResultsMessage
    );

    // Zatraži rezultate za trenutno pitanje
    getTempResults(question.id);

    return () => {
      if (stompClientRef.current?.connected) {
        finalResultsSub.unsubscribe();
      }
    };
  }, [quizCode, showResults, currentQuestionIndex, quiz]);

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

  const connectToWebSocket = () => {
    const socket = new SockJS(`${API_BASE_URL}/stomp-endpoint`);
    const client = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000,
      onConnect: async () => {
        console.log('Moderator connected to WebSocket');

        try {
          const response = await fetch(`${API_BASE_URL}/quiz-instance/${quizId}/start`, {
            method: 'POST',
          });
          const data = await response.json();
          setQuizCode(data.quizCode);
          localStorage.setItem('quizCode', data.quizCode);

          client.subscribe(`/topic/quiz/${data.quizCode}/participants`, handleParticipantsMessage);
          client.subscribe(`/topic/quiz/${data.quizCode}/showResults`, handleShowResultsMessage);
          client.subscribe(`/topic/quiz/${data.quizCode}/state`, handleStateMessage);

          // Traži trenutno stanje, koje su u /state postavi
          client.publish({
            destination: `/app/quiz/${data.quizCode}/getState`, // trigera state
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
    const storedCode = localStorage.getItem('quizCode');
    if (storedCode) {
      setQuizCode(storedCode);

      const socket = new SockJS(`${API_BASE_URL}/stomp-endpoint`);
      const client = new Client({
        webSocketFactory: () => socket,
        reconnectDelay: 5000,
        onConnect: () => {
          console.log('Reconnected to WebSocket with existing quiz code');

          client.subscribe(`/topic/quiz/${storedCode}/participants`, handleParticipantsMessage);
          client.subscribe(`/topic/quiz/${storedCode}/showResults`, handleShowResultsMessage);
          client.subscribe(`/topic/quiz/${storedCode}/state`, handleStateMessage);

          // Traži trenutno stanje, koje su u /state postavi
          client.publish({
            destination: `/app/quiz/${storedCode}/getState`, // trigera state
            body: JSON.stringify({}),
          });
        },
      });

      client.activate();
      stompClientRef.current = client;

      return () => {
        if (client.connected) {
          client.deactivate();
        }
      };
    }
  }, []);

  const startQuestion = () => {
    console.log('start');
    if (!stompClientRef.current?.connected || !quizCode || !quiz) {
      console.log('return');
      return;
    }

    const question = quiz.questions[currentQuestionIndex];
    if (!question) {
      console.log('no question');
      return;
    }

    stompClientRef.current.publish({
      destination: `/app/quiz/${quizCode}/question`,
      body: JSON.stringify(question),
    });

    // Zatraži rezultate za trenutno pitanje
    getTempResults(question.id);

    // Uvećaj index pitanja za sljedeći put
    setCurrentQuestionIndex(prev => prev + 1);
    setShowResults(false);
    setResultsStat([]);
    console.log('started');
  };

  const showVotingResults = () => {
    if (!stompClientRef.current?.connected || !quizCode || !quiz) return;
    const question = quiz.questions[currentQuestionIndex - 1];
    if (!question) return;

    // Prikaži rezultate i sudionicima
    stompClientRef.current.publish({
      destination: `/app/quiz/${quizCode}/finalResults`,
      body: JSON.stringify(question.id),
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
      const response = await fetch(`${API_BASE_URL}/quiz-instance/${quizCode}/end`, {
        method: 'POST',
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

  if (!quiz) return <p>Loading...</p>;

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
            onClick={connectToWebSocket}
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
              currentQuestionIndex < quiz.questions.length && ( // ako je true, gumb se peiakzuje i postavlja na false
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
        currentQuestionIndex > 0 && ( // ovo je live statistika, te krajnja statistika i točan odgovor
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
