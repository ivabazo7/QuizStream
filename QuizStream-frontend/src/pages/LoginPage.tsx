import { Button, Form, Input, Card } from 'antd';
import { useAuth } from '../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';

function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();

  const onFinish = async (values: { email: string; password: string }) => {
    const userData = await login(values.email, values.password);
    navigate(`/${userData?.id}/quiz`);
  };

  return (
    <Card title="Prijava" style={{ width: 400, margin: '100px auto' }}>
      <Form onFinish={onFinish} layout="vertical">
        <Form.Item
          label="Email"
          name="email"
          rules={[{ required: true, type: 'email', message: 'Unesite valjani email' }]}
        >
          <Input />
        </Form.Item>
        <Form.Item
          label="Lozinka"
          name="password"
          rules={[{ required: true, message: 'Unesite lozinku' }]}
        >
          <Input.Password />
        </Form.Item>
        <Form.Item>
          <Button type="primary" htmlType="submit" block>
            Prijava
          </Button>
        </Form.Item>
      </Form>
    </Card>
  );
}

export default LoginPage;
