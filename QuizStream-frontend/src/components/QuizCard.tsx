import { Card, Popconfirm } from 'antd';
import { DeleteOutlined } from '@ant-design/icons'; // Import delete icon
import { useNavigate } from 'react-router-dom';

const { Meta } = Card;

type QuizCardProps = {
  moderatorId: string;
  quizId: number;
  name: string;
  description: string | undefined;
  updatedAt: Date;
  onDelete(): void;
};

function QuizCard({ moderatorId, quizId, name, description, updatedAt, onDelete }: QuizCardProps) {
  const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;
  const navigate = useNavigate();

  const handleCardClick = () => {
    navigate(`/${moderatorId}/quiz/${quizId}`);
  };

  const handleDelete = async () => {
    try {
      const response = await fetch(`${API_BASE_URL}/quiz/${quizId}`, {
        method: 'DELETE',
      });

      if (response.ok) {
        onDelete();
      } else {
        console.error('Delete error:', await response.text());
      }
    } catch (error) {
      console.error('Delete error:', error);
    }
  };

  return (
    <Card
      title={name}
      style={{ margin: '20px auto', maxWidth: 800 }}
      hoverable
      onClick={handleCardClick}
      extra={
        <Popconfirm
          title="Are you sure you want to delete this quiz?"
          onConfirm={e => {
            e?.stopPropagation();
            handleDelete();
          }}
          onCancel={e => e?.stopPropagation()}
          okText="Yes"
          cancelText="No"
        >
          <DeleteOutlined
            onClick={e => e.stopPropagation()}
            style={{ color: 'red', fontSize: '16px' }}
          />
        </Popconfirm>
      }
    >
      <Meta description={`Last update: ${new Date(updatedAt).toLocaleString()}`} />
      <br />
      {description}
    </Card>
  );
}

export default QuizCard;
