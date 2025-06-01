import { Card, List, Spin } from 'antd';
import Title from 'antd/es/typography/Title';
import { Answer, AnswerResultStat } from '../types/quiz';

type Props = {
  quizName?: string;
  questionText: string;
  questionAnswerOptions: Answer[];
  resultsStat: AnswerResultStat[] | null;
};

function QuestionResultStatCard({
  quizName,
  questionText,
  questionAnswerOptions,
  resultsStat,
}: Props) {
  if (resultsStat === undefined || resultsStat === null || resultsStat.length === 0) {
    return <Spin></Spin>;
  }

  return (
    <Card title="Rezultati" style={{ marginTop: '1rem' }}>
      {quizName && <Title level={2}>{quizName}</Title>}
      <Title level={4}>{questionText}</Title>
      <List
        dataSource={questionAnswerOptions}
        renderItem={option => {
          const result = resultsStat.find(r => r.answerId == option.id);
          const style = result?.correct
            ? {
                backgroundColor: 'rgb(119, 238, 119)',
              }
            : {};
          return (
            <List.Item style={style}>
              {option.text} — {result?.count || 0} glasova ({result?.percentage || 0}%)
            </List.Item>
          );
        }}
      />
    </Card>
  );
}

export default QuestionResultStatCard;
