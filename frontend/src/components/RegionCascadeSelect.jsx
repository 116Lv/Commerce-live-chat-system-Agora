// 시/도 -> 시/군/구 -> 읍/면/동 순서로 좁혀가며 지역 하나를 골라 추가하는 컴포넌트
import { useEffect, useState } from 'react';
import { Button, Col, Form, Row } from 'react-bootstrap';
import { getDongList, getSidoList, getSigunguList } from '../api/regionApi.js';

export default function RegionCascadeSelect({ onAdd, disabled = false, allowPartial = false }) {
  const [sidoList, setSidoList] = useState([]);
  const [sigunguList, setSigunguList] = useState([]);
  const [dongList, setDongList] = useState([]);
  const [sido, setSido] = useState('');
  const [sigungu, setSigungu] = useState('');
  const [dongId, setDongId] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    getSidoList()
      .then((data) => setSidoList(Array.isArray(data) ? data : []))
      .catch((err) => setError(err.message));
  }, []);

  useEffect(() => {
    setSigungu('');
    setSigunguList([]);
    setDongId('');
    setDongList([]);

    if (!sido) {
      return;
    }

    getSigunguList(sido)
      .then((data) => setSigunguList(Array.isArray(data) ? data : []))
      .catch((err) => setError(err.message));
  }, [sido]);

  useEffect(() => {
    setDongId('');
    setDongList([]);

    if (!sido || !sigungu) {
      return;
    }

    getDongList(sido, sigungu)
      .then((data) => setDongList(Array.isArray(data) ? data : []))
      .catch((err) => setError(err.message));
  }, [sido, sigungu]);

  const handleAdd = () => {
    const dong = dongList.find((region) => String(region.regionId) === dongId);
    if (!dong) {
      return;
    }

    onAdd(dong);
    setDongId('');
  };

  const handleAddSido = () => {
    if (!sido) {
      return;
    }

    onAdd({ regionId: null, name: `${sido} 전체`, sido, sigungu: '', eupmyeondong: '' });
  };

  const handleAddSigungu = () => {
    if (!sido || !sigungu) {
      return;
    }

    onAdd({ regionId: null, name: `${sido} ${sigungu} 전체`, sido, sigungu, eupmyeondong: '' });
  };

  return (
    <div className="region-cascade-select">
      {error ? <p className="text-danger mb-2">{error}</p> : null}
      <Row className="g-2 align-items-end">
        <Col xs={12} sm={4}>
          <Form.Group controlId="region-cascade-sido">
            <Form.Label>시/도</Form.Label>
            <Form.Select value={sido} onChange={(event) => setSido(event.target.value)} disabled={disabled}>
              <option value="">선택</option>
              {sidoList.map((value) => (
                <option key={value} value={value}>
                  {value}
                </option>
              ))}
            </Form.Select>
          </Form.Group>
        </Col>
        <Col xs={12} sm={4}>
          <Form.Group controlId="region-cascade-sigungu">
            <Form.Label>시/군/구</Form.Label>
            <Form.Select
              value={sigungu}
              onChange={(event) => setSigungu(event.target.value)}
              disabled={disabled || !sido}
            >
              <option value="">선택</option>
              {sigunguList.map((value) => (
                <option key={value} value={value}>
                  {value}
                </option>
              ))}
            </Form.Select>
          </Form.Group>
        </Col>
        <Col xs={12} sm={4}>
          <Form.Group controlId="region-cascade-dong">
            <Form.Label>읍/면/동</Form.Label>
            <Form.Select
              value={dongId}
              onChange={(event) => setDongId(event.target.value)}
              disabled={disabled || !sigungu}
            >
              <option value="">선택</option>
              {dongList.map((region) => (
                <option key={region.regionId} value={region.regionId}>
                  {region.eupmyeondong}
                </option>
              ))}
            </Form.Select>
          </Form.Group>
        </Col>
      </Row>
      <div className="d-grid mt-2 gap-2">
        {allowPartial && sido && !sigungu ? (
          <Button type="button" variant="outline-secondary" onClick={handleAddSido} disabled={disabled}>
            {sido} 전체로 검색
          </Button>
        ) : null}
        {allowPartial && sido && sigungu && !dongId ? (
          <Button type="button" variant="outline-secondary" onClick={handleAddSigungu} disabled={disabled}>
            {sido} {sigungu} 전체로 검색
          </Button>
        ) : null}
        <Button type="button" variant="outline-primary" onClick={handleAdd} disabled={disabled || !dongId}>
          지역 추가
        </Button>
      </div>
    </div>
  );
}
