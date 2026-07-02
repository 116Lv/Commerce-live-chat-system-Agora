import { useEffect, useState } from 'react';
import { Alert, Button, Form } from 'react-bootstrap';
import LoadingState from '../components/LoadingState.jsx';
import ErrorState from '../components/ErrorState.jsx';
import EmptyState from '../components/EmptyState.jsx';
import RegionCascadeSelect from '../components/RegionCascadeSelect.jsx';
import { getMyCoupons } from '../api/couponApi.js';
import { changePassword, getMe, updateProfile } from '../api/mypageApi.js';
import { updatePreferredRegions } from '../api/regionApi.js';
import { useAuth } from '../auth/AuthContext.jsx';
import { formatCouponMoney, sortMyCoupons } from './couponUtils.js';
import { PageHeader, getPageContent, useApiResource } from './pageUtils.jsx';

const PASSWORD_MIN_LENGTH = 8;
const MIN_REGION_COUNT = 3;
const MAX_REGION_COUNT = 5;

const toRegionId = (region) => region?.regionId ?? region?.id;

const getPrimaryRegion = (regions = []) => regions.find((region) => region.primaryRegion) || regions[0] || null;

function SmileScoreRing({ score }) {
  const safeScore = Math.max(0, Math.min(100, Number(score) || 0));

  return (
    <div className="mypage-smile-ring" style={{ '--score': `${safeScore}%` }} aria-label={`스마일지수 ${safeScore}점`}>
      <strong>{safeScore}</strong>
      <span>스마일지수</span>
    </div>
  );
}

function CouponPreview({ coupons, loading, error }) {
  const previewCoupons = sortMyCoupons(coupons);

  return (
    <section className="mypage-panel mypage-coupon-panel" aria-labelledby="mypage-coupon-title">
      <h2 id="mypage-coupon-title">받은 쿠폰</h2>
      {loading ? <p className="mypage-muted">쿠폰 불러오는 중</p> : null}
      {error ? <p className="mypage-muted">쿠폰을 불러오지 못했어요</p> : null}
      {!loading && !error && previewCoupons.length === 0 ? (
        <div className="mypage-coupon-row">
          <span>보유 쿠폰이 없어요</span>
        </div>
      ) : null}
      {!loading && !error && previewCoupons.length > 0 ? (
        <div className="mypage-coupon-list">
          {previewCoupons.map((coupon, index) => (
            <div className="mypage-coupon-row" key={coupon.couponId ?? coupon.id ?? `${coupon.eventName || coupon.name}-${index}`}>
              <span>{coupon.eventName || coupon.name || '이름 없는 쿠폰'}</span>
              <strong>{formatCouponMoney(coupon.discountAmount)}</strong>
            </div>
          ))}
        </div>
      ) : null}
    </section>
  );
}

export default function MyPage() {
  const { updateUserProfile } = useAuth();
  const profileState = useApiResource(() => getMe(), []);
  const couponsState = useApiResource(() => getMyCoupons(), []);
  const coupons = getPageContent(couponsState.data);
  const profile = profileState.data;

  const [nickname, setNickname] = useState('');
  const [phone, setPhone] = useState('');
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [selectedRegions, setSelectedRegions] = useState([]);
  const [primaryRegionId, setPrimaryRegionId] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!profile) {
      return;
    }

    setNickname(profile.nickname || '');
    setPhone(profile.phone || '');

    const preferredRegions = profile.preferredRegions || [];
    setSelectedRegions(
      preferredRegions
        .map((region) => ({ ...region, regionId: toRegionId(region) }))
        .filter((region) => region.regionId)
    );
    const primary = getPrimaryRegion(preferredRegions);
    setPrimaryRegionId(primary ? String(toRegionId(primary)) : '');
  }, [profile]);

  const primaryRegion = getPrimaryRegion(profile?.preferredRegions);

  const handleAddRegion = (region) => {
    setError('');
    setSelectedRegions((current) => {
      if (current.some((item) => item.regionId === region.regionId)) {
        setError('이미 선택한 지역이에요.');
        return current;
      }

      if (current.length >= MAX_REGION_COUNT) {
        setError(`선호지역은 최대 ${MAX_REGION_COUNT}개까지 선택할 수 있습니다.`);
        return current;
      }

      const next = [...current, region];
      if (!primaryRegionId) {
        setPrimaryRegionId(String(region.regionId));
      }
      return next;
    });
  };

  const handleRemoveRegion = (regionId) => {
    setError('');
    setSelectedRegions((current) => {
      const next = current.filter((region) => region.regionId !== regionId);
      if (primaryRegionId === String(regionId)) {
        setPrimaryRegionId(next[0] ? String(next[0].regionId) : '');
      }
      return next;
    });
  };

  const validateForm = () => {
    if (
      selectedRegions.length > 0 &&
      (selectedRegions.length < MIN_REGION_COUNT || selectedRegions.length > MAX_REGION_COUNT)
    ) {
      return `선호지역은 ${MIN_REGION_COUNT}개 이상 ${MAX_REGION_COUNT}개 이하로 선택해주세요.`;
    }

    if (selectedRegions.length > 0 && !primaryRegionId) {
      return '대표 선호지역을 선택해주세요.';
    }

    if ((currentPassword || newPassword) && (!currentPassword || !newPassword)) {
      return '비밀번호를 변경하려면 현재 비밀번호와 새 비밀번호를 모두 입력해주세요.';
    }

    if (newPassword && newPassword.length < PASSWORD_MIN_LENGTH) {
      return `새 비밀번호는 ${PASSWORD_MIN_LENGTH}자 이상 입력해주세요.`;
    }

    return '';
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    setSubmitting(true);
    setMessage('');
    setError('');

    const validationMessage = validateForm();
    if (validationMessage) {
      setError(validationMessage);
      setSubmitting(false);
      return;
    }

    try {
      const updatedProfile = await updateProfile({ nickname, phone });
      updateUserProfile({ nickname: updatedProfile?.nickname || nickname, phone: updatedProfile?.phone || phone });

      if (selectedRegions.length > 0) {
        await updatePreferredRegions({
          regionIds: selectedRegions.map((region) => region.regionId),
          primaryRegionId: Number(primaryRegionId)
        });
      }

      if (currentPassword && newPassword) {
        await changePassword({ currentPassword, newPassword });
        setCurrentPassword('');
        setNewPassword('');
      }

      setMessage('프로필을 수정했어요.');
      await profileState.reload();
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <section className="mypage-profile-edit">
      <PageHeader title="내 정보 변경" eyebrow="마이페이지" />
      {profileState.loading ? <LoadingState label="내 정보 불러오는 중" /> : null}
      {profileState.error ? (
        <ErrorState title="내 정보를 불러오지 못했어요" message={profileState.error.message} onRetry={profileState.reload} />
      ) : null}
      {!profileState.loading && !profileState.error && !profile ? <EmptyState title="내 정보가 없어요" /> : null}
      {!profileState.loading && !profileState.error && profile ? (
        <div className="mypage-edit-grid">
          <div className="mypage-side-stack">
            <section className="mypage-panel mypage-profile-summary" aria-labelledby="mypage-profile-name">
              <SmileScoreRing score={profile.smileScore} />
              <h2 id="mypage-profile-name">{profile.nickname}</h2>
              <p>{primaryRegion?.name || '선호지역 미설정'} · 활동 회원</p>
            </section>
            <CouponPreview coupons={coupons} loading={couponsState.loading} error={couponsState.error} />
          </div>

          <Form className="mypage-panel mypage-edit-form" onSubmit={handleSubmit}>
            {message ? <Alert variant="success">{message}</Alert> : null}
            {error ? <Alert variant="danger">{error}</Alert> : null}
            <Form.Group className="mypage-field" controlId="mypage-current-password">
              <Form.Label>현재 비밀번호</Form.Label>
              <Form.Control
                type="password"
                value={currentPassword}
                onChange={(event) => setCurrentPassword(event.target.value)}
                placeholder="현재 비밀번호"
                autoComplete="current-password"
              />
            </Form.Group>
            <Form.Group className="mypage-field" controlId="mypage-new-password">
              <Form.Label>새 비밀번호</Form.Label>
              <Form.Control
                type="password"
                value={newPassword}
                onChange={(event) => setNewPassword(event.target.value)}
                placeholder="8자 이상 입력해주세요"
                autoComplete="new-password"
              />
            </Form.Group>
            <Form.Group className="mypage-field" controlId="mypage-nickname">
              <Form.Label>닉네임</Form.Label>
              <Form.Control
                value={nickname}
                onChange={(event) => setNickname(event.target.value)}
                placeholder="동네에서 사용할 닉네임"
                required
              />
            </Form.Group>
            <Form.Group className="mypage-field" controlId="mypage-phone">
              <Form.Label>휴대폰 번호</Form.Label>
              <Form.Control
                value={phone}
                onChange={(event) => setPhone(event.target.value)}
                placeholder="010 - 0000 - 0000"
                inputMode="tel"
                autoComplete="tel"
              />
            </Form.Group>
            <Form.Group className="mypage-field" controlId="mypage-regions">
              <Form.Label>선호지역</Form.Label>
              <RegionCascadeSelect onAdd={handleAddRegion} disabled={submitting || selectedRegions.length >= MAX_REGION_COUNT} />
              {selectedRegions.length === 0 ? (
                <EmptyState title="아직 선택한 지역이 없어요" />
              ) : (
                <div className="stack-list mt-2">
                  {selectedRegions.map((region) => (
                    <div className="d-flex align-items-center justify-content-between gap-3" key={region.regionId}>
                      <span>{region.name}</span>
                      <div className="d-flex align-items-center gap-2">
                        <Form.Check
                          type="radio"
                          id={`mypage-primary-region-${region.regionId}`}
                          name="mypagePrimaryRegionId"
                          label="대표"
                          value={region.regionId}
                          checked={primaryRegionId === String(region.regionId)}
                          onChange={(event) => setPrimaryRegionId(event.target.value)}
                        />
                        <Button
                          type="button"
                          variant="outline-secondary"
                          size="sm"
                          onClick={() => handleRemoveRegion(region.regionId)}
                        >
                          삭제
                        </Button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </Form.Group>
            <Button className="mypage-submit-button" type="submit" disabled={submitting}>
              {submitting ? '수정 중' : '수정'}
            </Button>
          </Form>
        </div>
      ) : null}
    </section>
  );
}
