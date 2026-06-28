import { Container, Nav, Navbar } from 'react-bootstrap';
import { Link, NavLink, Outlet } from 'react-router-dom';
import { MessageCircle, PackagePlus, Search, Ticket, UserRound } from 'lucide-react';

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
                <span>Products</span>
              </UserNavLink>
              <UserNavLink to="/sell">
                <PackagePlus size={17} aria-hidden="true" />
                <span>Sell</span>
              </UserNavLink>
              <UserNavLink to="/events">
                <Ticket size={17} aria-hidden="true" />
                <span>Events</span>
              </UserNavLink>
              <UserNavLink to="/chat">
                <MessageCircle size={17} aria-hidden="true" />
                <span>Chat</span>
              </UserNavLink>
            </Nav>
            <Nav className="align-items-lg-center gap-lg-1">
              <UserNavLink to="/me">
                <UserRound size={17} aria-hidden="true" />
                <span>My Page</span>
              </UserNavLink>
              <UserNavLink to="/login">Login</UserNavLink>
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
