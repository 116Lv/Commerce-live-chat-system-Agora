import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { test } from 'node:test';

const readPage = (name) => readFileSync(new URL(`./${name}`, import.meta.url), 'utf8');
const readSource = (path) => readFileSync(new URL(path, import.meta.url), 'utf8');

test('sell product page always offers a region picker with a cascading fallback', () => {
  const source = readPage('SellProductPage.jsx');

  assert.match(source, /import RegionPicker from '\.\.\/components\/RegionPicker\.jsx';/);
  assert.match(source, /<RegionPicker/);
});

test('my page renders an empty state when the profile payload is missing', () => {
  const source = readPage('MyPage.jsx');

  assert.match(source, /import EmptyState from '\.\.\/components\/EmptyState\.jsx';/);
  assert.match(source, /!profileState\.loading && !profileState\.error && !profile/);
});

test('my page replaces menu tiles with a profile edit layout', () => {
  const source = readPage('MyPage.jsx');
  const styles = readSource('../styles/theme.css');

  assert.doesNotMatch(source, /MetricTile/);
  assert.doesNotMatch(source, /to="\/me\/likes"|to="\/me\/products"|to="\/me\/trades"|to="\/me\/reviews"|to="\/me\/coupons"/);
  assert.match(source, /<PageHeader title="내 정보 변경" eyebrow="마이페이지" \/>/);
  assert.match(source, /className="mypage-edit-grid"/);
  assert.match(source, /changePassword/);
  assert.match(source, /updatePreferredRegions/);
  assert.match(source, /const \{ updateUserProfile \} = useAuth\(\);/);
  assert.match(source, /updateUserProfile\(\{ nickname: updatedProfile\?\.nickname \|\| nickname/);
  assert.doesNotMatch(source, /usableCoupons\[0\] \|\| coupons\[0\]/);
  assert.doesNotMatch(source, /toCouponFilter/);
  assert.match(source, /const previewCoupons = sortMyCoupons\(coupons\);/);
  assert.match(source, /previewCoupons\.map/);
  assert.match(styles, /\.mypage-edit-grid/);
  assert.match(styles, /\.mypage-smile-ring/);
  assert.match(styles, /\.mypage-coupon-list/);
});

test('product detail protects like action and does not expose edit without owner data', () => {
  const source = readPage('ProductDetailPage.jsx');

  assert.match(source, /import \{ useAuth \} from '\.\.\/auth\/AuthContext\.jsx';/);
  assert.match(source, /const \{ isUserAuthenticated \} = useAuth\(\);/);
  assert.match(source, /if \(!isUserAuthenticated\) \{/);
  assert.match(source, /navigate\('\/login'/);
  assert.doesNotMatch(source, /\/edit/);
});

test('coupon event issue action redirects unauthenticated users to login', () => {
  const source = readPage('CouponEventsPage.jsx');

  assert.match(source, /import \{ useAuth \} from '\.\.\/auth\/AuthContext\.jsx';/);
  assert.match(source, /const \{ isUserAuthenticated \} = useAuth\(\);/);
  assert.match(source, /if \(!isUserAuthenticated\) \{/);
  assert.match(source, /navigate\('\/login'/);
});

test('trade detail only exposes user-authorized trade actions', () => {
  const source = readPage('TradeDetailPage.jsx');

  assert.doesNotMatch(source, /expireReservation/);
  assert.doesNotMatch(source, /requestRatingMessage/);
  assert.match(source, /canCheckout/);
  assert.match(source, /canCompleteTrade/);
  assert.match(source, /isCurrentUserBuyer/);
  assert.match(source, /const canReviewTrade = isCurrentUserBuyer && tradeStatus === 'COMPLETED';/);
  assert.doesNotMatch(source, /const canReviewTrade = isCurrentUserParticipant && tradeStatus === 'COMPLETED';/);
});

test('user layout hides protected navigation while logged out and shows signup', () => {
  const source = readSource('../layouts/UserLayout.jsx');

  assert.match(source, /useAuth/);
  assert.match(source, /isUserAuthenticated/);
  assert.match(source, /회원가입/);
  assert.match(source, /로그아웃/);
  assert.match(source, /로그인 연장하기/);
  assert.match(source, /extendUserSession/);
  assert.match(source, /to: '\/me\/reviews', label: '후기'/);
  assert.match(source, /isUserAuthenticated \? \(/);
  assert.match(source, /<NavDropdown[\s\S]*?account-dropdown[\s\S]*?\)\s*:\s*\(\s*<>[\s\S]*?<UserNavLink to="\/login">/);
  assert.doesNotMatch(source, /<UserNavLink to="\/me">/);
});

test('home page uses Korean marketplace grid with sidebar filters', () => {
  const source = readPage('UserPlaceholderPages.jsx');

  assert.match(source, /title="상품 둘러보기"/);
  assert.match(source, /검색 조건/);
  assert.match(source, /상품 그리드/);
  assert.match(source, /getProducts/);
  assert.doesNotMatch(source, /label="Products"|label="Events"|label="Chat"/);
  assert.doesNotMatch(source, /No featured products yet/);
});

test('auth and admin chrome use Korean labels', () => {
  const userLogin = readSource('../features/auth/UserLoginPage.jsx');
  const userSignup = readSource('../features/auth/UserSignupPage.jsx');
  const adminLogin = readSource('../features/auth/AdminLoginPage.jsx');
  const adminLayout = readSource('../layouts/AdminLayout.jsx');

  assert.match(userLogin, /로그인/);
  assert.match(userSignup, /회원가입/);
  assert.match(adminLogin, /관리자 로그인/);
  assert.match(adminLayout, /대시보드/);
  assert.doesNotMatch(userLogin, /<h1>Login<\/h1>/);
  assert.doesNotMatch(userSignup, /Already have an account/);
  assert.doesNotMatch(adminLogin, /Admin Login/);
  assert.doesNotMatch(adminLayout, /label: 'Dashboard'|label: 'Products'|label: 'Users'|label: 'Reports'|label: 'Payments'|label: 'Coupons'/);
});

test('signup collects phone and sends new users to region setup', () => {
  const userSignup = readSource('../features/auth/UserSignupPage.jsx');

  assert.match(userSignup, /phone: ''/);
  assert.match(userSignup, /name="phone"/);
  assert.match(userSignup, /autoComplete="tel"/);
  assert.match(userSignup, /휴대폰 번호/);
  assert.match(userSignup, /navigate\('\/regions\/setup'/);
});

test('region setup page lets users cascade sido/sigungu/dong to choose preferred and primary regions', () => {
  const source = readPage('UserPlaceholderPages.jsx');

  assert.match(source, /import RegionCascadeSelect from '\.\.\/components\/RegionCascadeSelect\.jsx';/);
  assert.match(source, /updatePreferredRegions/);
  assert.match(source, /selectedRegions/);
  assert.match(source, /primaryRegionId/);
  assert.match(source, /시\/도, 시\/군\/구, 읍\/면\/동 순서로 관심 지역을 3개 이상 5개 이하로 선택해 주세요\./);
  assert.match(source, /<RegionCascadeSelect onAdd=\{handleAddRegion\}/);
  assert.match(source, /type="radio"/);
  assert.match(source, /관심 지역 저장/);
});
