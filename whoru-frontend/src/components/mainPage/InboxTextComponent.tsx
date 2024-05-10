import styles from './InboxTextComponent.module.css'
import ulIcon from '../../assets/components/InboxTextComponent/text-component-ul-button.svg'
import sqIcon from '../../assets/components/InboxTextComponent/text-component-sq-button.svg'
import xIcon from '../../assets/components/InboxTextComponent/text-component-x-button.svg'
import { MessageInfoDetail } from '../../types/mainTypes'
import { useDispatch } from 'react-redux';
import { useNavigate } from 'react-router-dom'
import { setReplyMessage } from '@/stores/storeMessageId';
import axios from 'axios'


interface InboxTextComponentProps extends React.HTMLAttributes<HTMLDivElement>{
  message: MessageInfoDetail;
  innerRef?: React.Ref<HTMLDivElement>;
}

const InboxTextComponent: React.FC<InboxTextComponentProps> = ({ message, innerRef, ...props }) => {
  // const [content, setContent] = useState<string>("")
  const dispatch = useDispatch()
  const navigate = useNavigate()
  // const messageId = useSelector((state: any) => state.reply.messageId);
  const accessToken = localStorage.getItem('AccessToken');

  const replyButtonStyle = message.responseStatus ?  {backgroundColor: 'gray'} : {}
  const reportButtonStyle = message.isReported ? { backgroundColor: 'gray' } : {}

  const handleReply = (messageId:number) => {
    dispatch(setReplyMessage(messageId));
    navigate('/post');
  };

  const handleReport = (messageId:number, senderId:number) => {
    if (confirm('정말로 신고하시겠습니까?')) {
      axios.post('http://k10d203.p.ssafy.io/api/report/member',
      {
        messageId: messageId,
        senderId: senderId,
      },
      {
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${accessToken}`
      }}
      )
      .then((res) => {
        console.log(res);
        alert('신고가 완료되었습니다.');
        window.location.reload();
      })
      .catch((err) => {
        console.log(err);
      })
    } 
  }

  const createDate = new Date(message.createDate);
  const now = new Date();

  const diffInMilliseconds = now.getTime() - createDate.getTime();
  const diffInSeconds = Math.floor(diffInMilliseconds / 1000);
  const diffInMinutes = Math.floor(diffInSeconds / 60);
  const diffInHours = Math.floor(diffInMinutes / 60);
  const diffInDays = Math.floor(diffInHours / 24);

  let timeFromNow = '';
  if (diffInDays > 0) {
    timeFromNow = `${diffInDays}일 전`;
  } else if (diffInHours > 0) {
    timeFromNow = `${diffInHours}시간 전`;
  } else if (diffInMinutes > 0) {
    timeFromNow = `${diffInMinutes}분 전`;
  } else {
    timeFromNow = `${diffInSeconds}초 전`;
  }
  


  return (
    <div className={styles.inboxTextComponent} key={message.id} ref={innerRef} {...props}>
      <div className={styles.inboxTextComponentHeader} key={message.id} {...props}>
        <div className={styles.inboxTextComponentHeaderText}>
          <p className={styles.inboxTextComponentHeaderTextTitle}>{message.responseStatus ? "답장 메세지" : "익명 메세지"}</p>
          <p className={styles.inboxTextComponentHeaderTime}>{timeFromNow}</p>
        </div>
        <div className={styles.inboxTextComponentHeaderIcons}>
          <img src={ulIcon} alt="ul-icon" />
          <img src={sqIcon} alt="sq-icon" />
          <img src={xIcon} alt="x-icon" />
        </div>
      </div>
      <div className={styles.inboxTextComponentBody}>
        <div className={styles.inboxTextComponentBodyMain}>
          <p className={styles.inboxTextComponentBodyMainText}>{message.content}</p>
            <div className={styles.inboxTextComponentFooter}>
              <button className={message.responseStatus ? styles.inboxTextComponentFooterButtonDisable : styles.inboxTextComponentFooterButton} 
                onClick={() => handleReply(message.id)}
                style={replyButtonStyle}
                disabled={message.responseStatus}
              >
                답장
              </button>
              <button className={styles.inboxTextComponentFooterReportButton} 
                onClick={() => handleReport(message.id, message.senderId)}
                style={reportButtonStyle}
                disabled={message.isReported}
              >
                신고
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

export default InboxTextComponent