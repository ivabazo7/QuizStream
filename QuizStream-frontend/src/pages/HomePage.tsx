import { Divider, Flex, Input, Typography, message } from 'antd';
import type { GetProps } from 'antd';
import GradientButton from '../components/ui/GradientButton';
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';

type OTPProps = GetProps<typeof Input.OTP>;
const { Title } = Typography;

function HomePage() {
  const [quizCode, setQuizCode] = useState<string | undefined>(undefined);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

  const onInput: OTPProps['onInput'] = value => {
    setQuizCode(value.join(''));
  };

  const handleJoinQuiz = async () => {
    console.log('Clicked ' + quizCode);
    if (!quizCode || quizCode.length != 6) {
      message.error('Please insert a valid 6-character quiz code!');
      return;
    }

    setLoading(true);
    try {
      // Provjeri je li kod valjan
      const response = await fetch(`${API_BASE_URL}/quiz-instance/validate-code/${quizCode}`);

      if (!response.ok) {
        throw new Error('Validation failed');
      }

      const isValid = await response.json();

      if (!isValid) {
        message.error('Invalid or expired quiz code!');
        return;
      }

      // Ako je kod valjan, preusmjeri na kviz
      navigate(`/quiz/${quizCode}`);
    } catch (error) {
      console.error('Error validating quiz code:', error);
      message.error('Failed to validate quiz code. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Flex
      gap="small"
      align="center"
      style={{
        minHeight: '100%',
        justifyContent: 'center',
      }}
      vertical
    >
      <Flex gap="small" align="center" vertical>
        <Title level={5}>Insert Code to Join Quiz:</Title>
        <Input.OTP
          formatter={str => str.toUpperCase()}
          length={6}
          separator={<span>-</span>}
          style={{ lineHeight: '8px' }}
          onInput={onInput}
        />
        <GradientButton
          text="Join"
          onClick={handleJoinQuiz}
          loading={loading}
          disabled={!quizCode || quizCode.length !== 6}
        />
      </Flex>
      <Divider plain>Or</Divider>
      <Flex gap="small" align="center" vertical>
        SignIn ili QR code
      </Flex>
    </Flex>
  );
}

export default HomePage;
