import { useEffect, useState } from 'react';
import { Quiz } from '../types/quiz';
import QuizCreateEditForm from '../components/QuizCreateEditForm';
import { Button } from 'antd';
import { useParams } from 'react-router-dom';
import QuizCard from '../components/QuizCard';

// /:moderatorId/quiz/:quizId
function QuizModeratorPage() {
  const { moderatorId } = useParams();
  const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

  const [quizzes, setQuizzes] = useState<Quiz[]>([]);
  const [showForm, setShowForm] = useState(false);

  const handleQuizCreated = (quiz: Quiz) => {
    setQuizzes(prev => [...prev, quiz]);
    setShowForm(false);
  };

  useEffect(() => {
    const fetchQuizzes = async () => {
      try {
        const response = await fetch(`${API_BASE_URL}/moderator/${moderatorId}/quiz`);
        if (!response.ok) throw new Error('Failed to fetch quizzes');
        const data = await response.json();
        setQuizzes(data);
      } catch (error) {
        console.error('Error fetching quizzes:', error);
      }
    };

    if (moderatorId) {
      fetchQuizzes();
    }
  }, [moderatorId]);

  return (
    <div>
      <Button type="primary" danger={showForm} onClick={() => setShowForm(prev => !prev)}>
        {showForm ? 'Cancel creating' : 'Create Quiz'}
      </Button>
      {showForm && <QuizCreateEditForm onQuizCreated={handleQuizCreated} />}
      <hr />
      <ul>
        {quizzes.map(
          (q, i) =>
            moderatorId && (
              <QuizCard
                key={i}
                moderatorId={moderatorId}
                quizId={q.id}
                name={q.name}
                description={q.description}
                updatedAt={q.updatedAt}
              />
            )
        )}
      </ul>
    </div>
  );
}

export default QuizModeratorPage;
