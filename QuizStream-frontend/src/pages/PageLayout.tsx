import { Link, Outlet } from 'react-router-dom';
import { Flex, Layout } from 'antd';
import { useAuth } from '../contexts/AuthContext';
const { Header, Footer, Content } = Layout;

function PageLayout() {
  const { user, logout } = useAuth();

  const layoutStyle: React.CSSProperties = {
    minHeight: '100vh',
  };

  const headerStyle: React.CSSProperties = {
    textAlign: 'center',
    color: '#fff',
    height: 64,
    paddingInline: 48,
    lineHeight: '64px',
    backgroundColor: '#3388f8',
  };

  const contentStyle: React.CSSProperties = {
    textAlign: 'center',
    lineHeight: '120px',
    color: '#fff',
  };

  const navStyle: React.CSSProperties = {
    display: 'flex',
    columnGap: '32px',
    justifyContent: 'center',
  };

  const footerStyle: React.CSSProperties = {
    textAlign: 'center',
    color: '#ffffffc0',
    backgroundColor: '#3388f8',
  };

  const linkStyle: React.CSSProperties = {
    fontWeight: 'bold',
    fontSize: '1rem',
  };

  return (
    <>
      <Flex gap="middle" wrap>
        <Layout style={layoutStyle}>
          <Header style={headerStyle}>
            <nav style={navStyle}>
              <Link style={linkStyle} to="/">
                Home
              </Link>
              {user ? (
                <>
                  <Link style={linkStyle} to={`/${user.id}/quiz/`}>
                    My Quizzes
                  </Link>
                  <Link style={linkStyle} to="/login" onClick={logout}>
                    Odjava
                  </Link>
                </>
              ) : (
                <>
                  <Link style={linkStyle} to="/register">
                    Register
                  </Link>
                  <Link style={linkStyle} to="/login">
                    Login
                  </Link>
                </>
              )}
            </nav>
          </Header>
          <Content style={contentStyle}>
            <Outlet />
          </Content>
          <Footer style={footerStyle}>© 2025 QuizStream. All rights reserved.</Footer>
        </Layout>
      </Flex>
    </>
  );
}

export default PageLayout;
