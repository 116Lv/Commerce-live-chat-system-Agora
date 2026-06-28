import { createBrowserRouter } from 'react-router-dom';
import UserLayout from '../layouts/UserLayout.jsx';
import AdminLayout from '../layouts/AdminLayout.jsx';
import {
  ChatPage,
  ChatRoomPage,
  CouponsPage,
  HomePage,
  LoginPage,
  MyCouponsPage,
  MyLikesPage,
  MyPage,
  MyProductsPage,
  MyReviewsPage,
  MyTradesPage,
  NotFoundPage,
  ProductDetailPage,
  ProductsPage,
  RegionSetupPage,
  SellPage,
  SignupPage
} from '../pages/UserPlaceholderPages.jsx';
import {
  AdminCouponsPage,
  AdminDashboardPage,
  AdminLoginPage,
  AdminNotFoundPage,
  AdminPaymentsPage,
  AdminProductsPage,
  AdminReportsPage,
  AdminUsersPage
} from '../pages/AdminPlaceholderPages.jsx';

export const router = createBrowserRouter([
  {
    path: '/admin/login',
    element: <AdminLoginPage />
  },
  {
    path: '/admin',
    element: <AdminLayout />,
    children: [
      { index: true, element: <AdminDashboardPage /> },
      { path: 'products', element: <AdminProductsPage /> },
      { path: 'users', element: <AdminUsersPage /> },
      { path: 'reports', element: <AdminReportsPage /> },
      { path: 'payments', element: <AdminPaymentsPage /> },
      { path: 'coupons', element: <AdminCouponsPage /> },
      { path: '*', element: <AdminNotFoundPage /> }
    ]
  },
  {
    path: '/',
    element: <UserLayout />,
    children: [
      { index: true, element: <HomePage /> },
      { path: 'login', element: <LoginPage /> },
      { path: 'signup', element: <SignupPage /> },
      { path: 'regions/setup', element: <RegionSetupPage /> },
      { path: 'products', element: <ProductsPage /> },
      { path: 'products/:productId', element: <ProductDetailPage /> },
      { path: 'sell', element: <SellPage /> },
      { path: 'events', element: <CouponsPage /> },
      { path: 'chat', element: <ChatPage /> },
      { path: 'chat/:chatRoomId', element: <ChatRoomPage /> },
      { path: 'me', element: <MyPage /> },
      { path: 'me/likes', element: <MyLikesPage /> },
      { path: 'me/products', element: <MyProductsPage /> },
      { path: 'me/trades', element: <MyTradesPage /> },
      { path: 'me/reviews', element: <MyReviewsPage /> },
      { path: 'me/coupons', element: <MyCouponsPage /> },
      { path: '*', element: <NotFoundPage /> }
    ]
  }
]);
