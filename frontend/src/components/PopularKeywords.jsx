import { useState } from 'react';
import { Badge, Nav } from 'react-bootstrap';
import { TrendingUp } from 'lucide-react';
import { getDailyPopularKeywords, getRealtimePopularKeywords, getWeeklyPopularKeywords } from '../api/searchApi.js';
import { getPageContent, useApiResource } from '../pages/pageUtils.jsx';

const TABS = [
  { key: 'realtime', label: '실시간', loader: getRealtimePopularKeywords },
  { key: 'daily', label: '일간', loader: getDailyPopularKeywords },
  { key: 'weekly', label: '주간', loader: getWeeklyPopularKeywords }
];

export default function PopularKeywords({ onKeywordSelect }) {
  const [activeTab, setActiveTab] = useState(TABS[0].key);
  const currentTab = TABS.find((tab) => tab.key === activeTab) ?? TABS[0];
  const keywordsState = useApiResource(() => currentTab.loader(10), [activeTab]);
  const keywords = getPageContent(keywordsState.data);

  return (
    <div className="toolbar-panel mb-4">
      <div className="d-flex align-items-center gap-2 mb-2">
        <TrendingUp size={17} aria-hidden="true" />
        <strong>인기 검색어</strong>
        <Nav variant="pills" activeKey={activeTab} onSelect={(key) => setActiveTab(key ?? TABS[0].key)} className="ms-auto">
          {TABS.map((tab) => (
            <Nav.Item key={tab.key}>
              <Nav.Link eventKey={tab.key}>{tab.label}</Nav.Link>
            </Nav.Item>
          ))}
        </Nav>
      </div>

      {keywordsState.loading ? <p className="text-muted mb-0">불러오는 중...</p> : null}
      {!keywordsState.loading && keywords.length === 0 ? <p className="text-muted mb-0">인기 검색어가 없어요</p> : null}
      {!keywordsState.loading && keywords.length > 0 ? (
        <div className="d-flex flex-wrap gap-2">
          {keywords.map((item, index) => (
            <Badge
              key={item.keyword}
              as="button"
              type="button"
              bg="light"
              text="dark"
              className="border"
              onClick={() => onKeywordSelect?.(item.keyword)}
            >
              {index + 1}. {item.keyword}
            </Badge>
          ))}
        </div>
      ) : null}
    </div>
  );
}
