import { createBrowserRouter } from 'react-router-dom';
import { RequireAdminAuth, RequireUserAuth } from '../auth/RouteGuards.jsx';
import AdminLoginPage from '../features/auth/AdminLoginPage.jsx';
import UserLoginPage from '../features/auth/UserLoginPage.jsx';
import UserSignupPage from '../features/auth/UserSignupPage.jsx';
import UserLayout from '../layouts/UserLayout.jsx';
import AdminLayout from '../layouts/AdminLayout.jsx';
import {
  ChatPage,
  ChatRoomPage,
  CouponsPage,
  HomePage,
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
  SellPage
} from '../pages/UserPlaceholderPages.jsx';
import {
  AdminCouponsPage,
  AdminDashboardPage,
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
    element: <RequireAdminAuth />,
    children: [
      {
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
      }
    ]
  },
  {
    path: '/',
    element: <UserLayout />,
    children: [
      { index: true, element: <HomePage /> },
      { path: 'login', element: <UserLoginPage /> },
      { path: 'signup', element: <UserSignupPage /> },
      { path: 'products', element: <ProductsPage /> },
      { path: 'products/:productId', element: <ProductDetailPage /> },
      { path: 'events', element: <CouponsPage /> },
      {
        element: <RequireUserAuth />,
        children: [
          { path: 'regions/setup', element: <RegionSetupPage /> },
          { path: 'sell', element: <SellPage /> },
          { path: 'chat', element: <ChatPage /> },
          { path: 'chat/:chatRoomId', element: <ChatRoomPage /> },
          { path: 'me', element: <MyPage /> },
          { path: 'me/likes', element: <MyLikesPage /> },
          { path: 'me/products', element: <MyProductsPage /> },
          { path: 'me/trades', element: <MyTradesPage /> },
          { path: 'me/reviews', element: <MyReviewsPage /> },
          { path: 'me/coupons', element: <MyCouponsPage /> }
        ]
      },
      { path: '*', element: <NotFoundPage /> }
    ]
  }
]);
