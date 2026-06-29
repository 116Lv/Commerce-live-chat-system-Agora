import { useCallback, useEffect, useMemo, useState } from 'react';
import { Alert, Button, Col, Form, Row } from 'react-bootstrap';
import { Link, useParams } from 'react-router-dom';
import { ImagePlus, Send } from 'lucide-react';
import { getMessages, markRoomRead, uploadChatImage } from '../api/chatApi.js';
import EmptyState from '../components/EmptyState.jsx';
import ErrorState from '../components/ErrorState.jsx';
import LoadingState from '../components/LoadingState.jsx';
import NegoPanel from '../features/nego/NegoPanel.jsx';
import useChatSocket from '../features/chat/useChatSocket.js';
import { PageHeader, formatDateTime, useApiResource } from './pageUtils.jsx';

const dedupeMessages = (messages) => {
  const byKey = new Map();

  messages.forEach((message, index) => {
    const key = message.messageId ?? `local-${index}-${message.createdAt ?? ''}-${message.content ?? ''}`;
    byKey.set(key, message);
  });

  return Array.from(byKey.values()).sort((a, b) => Number(a.messageId ?? 0) - Number(b.messageId ?? 0));
};

export default function ChatRoomPage() {
  const { chatRoomId } = useParams();
  const [messages, setMessages] = useState([]);
  const [draft, setDraft] = useState('');
  const [actionMessage, setActionMessage] = useState('');
  const [actionError, setActionError] = useState('');
  const [uploading, setUploading] = useState(false);
  const { data, error, loading, reload } = useApiResource(() => getMessages(chatRoomId, { size: 100 }), [chatRoomId]);

  useEffect(() => {
    if (Array.isArray(data)) {
      setMessages(data);
      markRoomRead(chatRoomId).catch(() => {});
    }
  }, [chatRoomId, data]);

  const appendMessage = useCallback((message) => {
    setMessages((current) => dedupeMessages([...current, message]));
  }, []);

  const socket = useChatSocket(chatRoomId, appendMessage);
  const renderedMessages = useMemo(() => dedupeMessages(messages), [messages]);

  const handleSend = (event) => {
    event.preventDefault();
    setActionError('');
    setActionMessage('');

    try {
      socket.sendMessage(draft.trim());
      setDraft('');
    } catch (err) {
      setActionError(err.message);
    }
  };

  const handleImage = async (event) => {
    const image = event.target.files?.[0];

    if (!image) {
      return;
    }

    setActionError('');
    setActionMessage('');
    setUploading(true);

    try {
      const response = await uploadChatImage(chatRoomId, image);
      appendMessage(response);
      setActionMessage('이미지를 보냈어요.');
    } catch (err) {
      setActionError(err.message);
    } finally {
      setUploading(false);
      event.target.value = '';
    }
  };

  return (
    <section>
      <PageHeader
        title={`채팅방 #${chatRoomId}`}
        eyebrow="메시지"
        action={
          <Button as={Link} to="/chat" variant="outline-primary">
            목록
          </Button>
        }
      />
      {socket.error ? <Alert variant="warning">실시간 연결 오류: {socket.error}</Alert> : null}
      {actionMessage ? <Alert variant="success">{actionMessage}</Alert> : null}
      {actionError ? <Alert variant="danger">{actionError}</Alert> : null}
      <Row className="g-3">
        <Col xs={12} lg={8}>
          <div className="detail-panel chat-room-panel">
            {loading ? <LoadingState label="메시지 불러오는 중" /> : null}
            {error ? <ErrorState title="메시지를 불러오지 못했어요" message={error.message} onRetry={reload} /> : null}
            {!loading && !error && renderedMessages.length === 0 ? <EmptyState title="메시지가 없어요" /> : null}
            {!loading && !error && renderedMessages.length > 0 ? (
              <div className="message-list">
                {renderedMessages.map((message) => (
                  <article key={message.messageId ?? `${message.createdAt}-${message.content}`} className="message-item">
                    <div className="message-meta">
                      <strong>{message.senderNickname || `사용자 ${message.senderId || '-'}`}</strong>
                      <span>{formatDateTime(message.createdAt)}</span>
                    </div>
                    {message.messageType === 'IMAGE' ? (
                      <a href={message.content} target="_blank" rel="noreferrer">
                        이미지 보기
                      </a>
                    ) : (
                      <p>{message.content}</p>
                    )}
                  </article>
                ))}
              </div>
            ) : null}
            <Form className="chat-compose" onSubmit={handleSend}>
              <Form.Control
                value={draft}
                onChange={(event) => setDraft(event.target.value)}
                placeholder={socket.connected ? '메시지 입력' : '연결 대기 중'}
                maxLength={1000}
                disabled={!socket.connected}
              />
              <Button type="submit" disabled={!socket.connected || !draft.trim()} aria-label="메시지 보내기">
                <Send size={17} aria-hidden="true" />
              </Button>
              <Button as="label" variant="outline-primary" aria-label="이미지 보내기" disabled={uploading}>
                <ImagePlus size={17} aria-hidden="true" />
                <Form.Control type="file" accept="image/*" className="visually-hidden" onChange={handleImage} />
              </Button>
            </Form>
          </div>
        </Col>
        <Col xs={12} lg={4}>
          <NegoPanel chatRoomId={chatRoomId} />
        </Col>
      </Row>
    </section>
  );
}
