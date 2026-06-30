import { Container, Nav, Navbar, NavDropdown } from 'react-bootstrap';
import { Link, NavLink, Outlet, useNavigate } from 'react-router-dom';
import { Heart, ListChecks, LogOut, MessageCircle, PackagePlus, Search, Ticket, UserRound } from 'lucide-react';
import { useAuth } from '../auth/AuthContext.jsx';

const ACCOUNT_LINKS = [
  { to: '/me', label: '내 정보', icon: UserRound },
  { to: '/me/products', label: '내 상품', icon: PackagePlus },
  { to: '/me/likes', label: '관심 상품', icon: Heart },
  { to: '/chat', label: '채팅', icon: MessageCircle },
  { to: '/me/trades', label: '거래 내역', icon: ListChecks }
];

function decodeTokenPayload(token) {
  if (!token || typeof globalThis.atob !== 'function') {
    return null;
  }

  try {
    const [, payload] = token.split('.');

    if (!payload) {
      return null;
    }

    const normalizedPayload = payload.replace(/-/g, '+').replace(/_/g, '/');
    const paddedPayload = normalizedPayload.padEnd(Math.ceil(normalizedPayload.length / 4) * 4, '=');
    const decodedPayload = globalThis.atob(paddedPayload);
    const json = decodeURIComponent(
      Array.from(decodedPayload, (character) => `%${character.charCodeAt(0).toString(16).padStart(2, '0')}`).join('')
    );

    return JSON.parse(json);
  } catch {
    return null;
  }
}

function getAccountLabel(userToken) {
  const payload = decodeTokenPayload(userToken);

  return payload?.nickname || payload?.name || payload?.email || '내 계정';
}

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
  const navigate = useNavigate();
  const { isUserAuthenticated, logoutUser, userToken } = useAuth();
  const accountLabel = getAccountLabel(userToken);

  const handleLogout = async () => {
    await logoutUser();
    navigate('/', { replace: true });
  };

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
                <NavDropdown
                  id="account-dropdown"
                  align="end"
                  className="account-dropdown"
                  title={
                    <span className="account-dropdown-toggle">
                      <UserRound size={18} aria-hidden="true" />
                      <span className="account-label">{accountLabel}</span>
                    </span>
                  }
                >
                  {ACCOUNT_LINKS.map(({ to, label, icon: Icon }) => (
                    <NavDropdown.Item key={to} as={NavLink} to={to}>
                      <Icon size={16} aria-hidden="true" />
                      <span>{label}</span>
                    </NavDropdown.Item>
                  ))}
                  <NavDropdown.Divider />
                  <NavDropdown.Item as="button" type="button" onClick={handleLogout}>
                    <LogOut size={16} aria-hidden="true" />
                    <span>로그아웃</span>
                  </NavDropdown.Item>
                </NavDropdown>
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
