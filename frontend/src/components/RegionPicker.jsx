// 선호지역을 우선 보여주고, 필요할 때만 전국 계단식 선택으로 넓히는 지역 선택 컴포넌트
import { useState } from 'react';
import { Button, Form } from 'react-bootstrap';
import RegionCascadeSelect from './RegionCascadeSelect.jsx';

const OTHER_VALUE = '__other__';

export default function RegionPicker({
  preferredRegions = [],
  value = '',
  valueLabel = '',
  onChange,
  allowAll = false,
  disabled = false
}) {
  const [pickingOther, setPickingOther] = useState(false);

  const isPreferredValue = preferredRegions.some((region) => String(region.regionId) === String(value));
  const hasOtherValue = Boolean(value) && !isPreferredValue;
  const selectValue = pickingOther || hasOtherValue ? OTHER_VALUE : value;

  const handleSelectChange = (event) => {
    const next = event.target.value;
    if (next === OTHER_VALUE) {
      setPickingOther(true);
      return;
    }

    setPickingOther(false);
    const region = preferredRegions.find((item) => String(item.regionId) === next);
    onChange(next, region?.name || '');
  };

  const handleCascadeAdd = (region) => {
    setPickingOther(false);
    if (region.regionId) {
      onChange(String(region.regionId), region.name);
      return;
    }

    const scopeValue = region.sigungu ? `sigungu:${region.sido}:${region.sigungu}` : `sido:${region.sido}`;
    onChange(scopeValue, region.name);
  };

  return (
    <div className="region-picker">
      <Form.Select value={selectValue} onChange={handleSelectChange} disabled={disabled}>
        <option value="">{allowAll ? '전체 지역' : '선택'}</option>
        {preferredRegions.map((region) => (
          <option key={region.regionId} value={region.regionId}>
            {region.name}
          </option>
        ))}
        <option value={OTHER_VALUE}>다른 지역 선택</option>
      </Form.Select>
      {pickingOther ? (
        <div className="mt-2">
          <RegionCascadeSelect onAdd={handleCascadeAdd} disabled={disabled} allowPartial={allowAll} />
        </div>
      ) : null}
      {!pickingOther && hasOtherValue ? (
        <div className="mt-2 d-flex align-items-center gap-2">
          <span>선택됨: {valueLabel}</span>
          <Button type="button" variant="link" size="sm" className="p-0" onClick={() => setPickingOther(true)}>
            변경
          </Button>
        </div>
      ) : null}
    </div>
  );
}
