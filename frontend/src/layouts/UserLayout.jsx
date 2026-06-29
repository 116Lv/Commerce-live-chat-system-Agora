import { Button, Container, Nav, Navbar } from 'react-bootstrap';
import { Link, NavLink, Outlet } from 'react-router-dom';
import { MessageCircle, PackagePlus, Search, Ticket, UserRound } from 'lucide-react';
import { useAuth } from '../auth/AuthContext.jsx';

function UserNavLink({ to, end, children }) {
  return (
    <NavLink
      to={to}
      end={end}
      className={({ isActive }) => `nav-link agora-nav-link${isActive ? ' active' : ''}`}
    >
      {children}
    </NavLink>
  );
}

export default function UserLayout() {
  const { isUserAuthenticated, logoutUser } = useAuth();

  return (
    <div className="user-shell">
      <Navbar expand="lg" className="agora-user-nav" sticky="top">
        <Container fluid="lg">
          <Navbar.Brand as={Link} to="/" className="agora-brand">
            Agora
          </Navbar.Brand>
          <Navbar.Toggle aria-controls="user-navigation" />
          <Navbar.Collapse id="user-navigation">
            <Nav className="me-auto align-items-lg-center gap-lg-1">
              <UserNavLink to="/products">
                <Search size={17} aria-hidden="true" />
                <span>상품</span>
              </UserNavLink>
              {isUserAuthenticated ? (
                <UserNavLink to="/sell">
                  <PackagePlus size={17} aria-hidden="true" />
                  <span>판매</span>
                </UserNavLink>
              ) : null}
              <UserNavLink to="/events">
                <Ticket size={17} aria-hidden="true" />
                <span>쿠폰</span>
              </UserNavLink>
              {isUserAuthenticated ? (
                <UserNavLink to="/chat">
                  <MessageCircle size={17} aria-hidden="true" />
                  <span>채팅</span>
                </UserNavLink>
              ) : null}
            </Nav>
            <Nav className="align-items-lg-center gap-lg-1">
              {isUserAuthenticated ? (
                <>
                  <UserNavLink to="/me">
                    <UserRound size={17} aria-hidden="true" />
                    <span>마이페이지</span>
                  </UserNavLink>
                  <Button type="button" variant="link" className="nav-link agora-nav-link" onClick={logoutUser}>
                    로그아웃
                  </Button>
                </>
              ) : (
                <>
                  <UserNavLink to="/login">로그인</UserNavLink>
                  <UserNavLink to="/signup">회원가입</UserNavLink>
                </>
              )}
            </Nav>
          </Navbar.Collapse>
        </Container>
      </Navbar>
      <main className="user-main">
        <Container fluid="lg" className="py-4 py-lg-5">
          <Outlet />
        </Container>
      </main>
    </div>
  );
}
