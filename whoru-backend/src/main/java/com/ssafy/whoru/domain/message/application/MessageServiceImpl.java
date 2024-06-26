package com.ssafy.whoru.domain.message.application;

import com.ssafy.whoru.domain.member.application.CrossMemberService;
import com.ssafy.whoru.domain.member.domain.FcmNotification;
import com.ssafy.whoru.domain.member.domain.Member;
import com.ssafy.whoru.domain.message.dao.MessageCustomRepository;
import com.ssafy.whoru.domain.message.dao.MessageRepository;
import com.ssafy.whoru.domain.message.dto.response.MessageResponse;
import com.ssafy.whoru.domain.message.dto.response.SendResponse;
import com.ssafy.whoru.domain.message.exception.AlreadyAllocatedException;
import com.ssafy.whoru.global.common.dto.FcmType;
import com.ssafy.whoru.global.common.dto.response.ResponseWithSuccess;
import com.ssafy.whoru.domain.message.dto.response.SliceMessageResponse;
import com.ssafy.whoru.global.common.application.S3Service;
import com.ssafy.whoru.global.common.dto.FileType;
import com.ssafy.whoru.domain.message.domain.Message;
import com.ssafy.whoru.domain.message.dto.ContentType;
import com.ssafy.whoru.domain.message.dto.request.TextSend;
import com.ssafy.whoru.domain.message.exception.BannedSenderException;
import com.ssafy.whoru.domain.message.exception.MessageNotFoundException;
import com.ssafy.whoru.domain.message.exception.ReportedMessageException;
import com.ssafy.whoru.domain.message.exception.UnacceptableFileTypeException;
import com.ssafy.whoru.domain.message.util.MessageUtil;
import com.ssafy.whoru.global.common.dto.RedisKeyType;
import com.ssafy.whoru.global.common.dto.SuccessType;
import com.ssafy.whoru.global.common.exception.S3UploadException;
import com.ssafy.whoru.global.util.FCMUtil;
import com.ssafy.whoru.global.util.RedisUtil;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@AllArgsConstructor
@Service
public class MessageServiceImpl implements MessageService{

    MessageRepository messageRepository;

    MessageCustomRepository messageCustomRepository;

    CrossMemberService memberService;

    ModelMapper modelMapper;

    S3Service s3Service;

    RedisUtil redisUtil;

    FCMUtil fcmUtil;

    MessageUtil messageUtil;
    static final Integer EMPTY_MESSAGE = 0;
    static final Integer BOX_PROBABILITY = 30;

    @Override
    @Transactional
    public SendResponse sendTextMessage(TextSend textSend, Long senderId) {

        // 정지여부 체크
        if(messageUtil.isBanned(senderId)){
            throw new BannedSenderException();
        }
        SendResponse sendResponse = new SendResponse(false);

        // db에서 두사람 정보 들고오기
        Member sender = memberService.findByIdToEntity(senderId);

        int randomNumber = ThreadLocalRandom.current().nextInt(100);

        if(randomNumber < BOX_PROBABILITY){
            // db상에 boxcount도 증가시켜야 하고
            sender.updateBoxIncrease();
            sendResponse.setRandomBoxProvided(true);
        }

        // message 전송
        messageRepository.save(
                Message.builder()
                        .content(textSend.getContent())
                        .contentType(ContentType.text)
                        .sender(sender)
                        .receiver(null)
                        .isReported(false)
                        .readStatus(false)
                        .parent(null)
                        .isResponse(false)
                        .responseStatus(false)
                        .build()
        );
        return sendResponse;
    }

    @Override
    @Transactional
    public void responseTextMessage(TextSend textSend, Long senderId, Long messageId) {

        if(messageUtil.isBanned(senderId)){
            throw new BannedSenderException();
        }

        Optional<Message> result = messageRepository.findById(messageId);
        Message targetMessage = result.orElseThrow(MessageNotFoundException::new);
        if(targetMessage.getIsReported()){
            throw new ReportedMessageException();
        }

        targetMessage.updateResponseStatus(true);

        Message responseMessage = Message.builder()
                .contentType(ContentType.text)
                .content(textSend.getContent())
                .sender(targetMessage.getReceiver())
                .receiver(targetMessage.getSender())
                .responseStatus(null)
                .parent(targetMessage)
                .readStatus(false)
                .isReported(false)
                .isResponse(true)
                .build();

        Member receiver = targetMessage.getSender();

        log.info("text response send -> sender: {}, receiver: {}", targetMessage.getReceiver().getUserName(), receiver.getUserName());

        if(receiver.getIsEnabled()){
            List<FcmNotification> fcmNotifications = receiver.getFcmNotifications();
            for(FcmNotification fcmNotification: fcmNotifications){
                if(fcmNotification == null) continue;
                if(fcmNotification.getMark()) continue;
                fcmUtil.sendMessage(fcmNotification.getFcmToken(), fcmNotification.getId(),FcmType.RESPONSE_MESSAGE);
            }
        }

        messageRepository.save(responseMessage);
    }

    @Override
    @Transactional
    public SendResponse sendMediaMessage(MultipartFile file, Long senderId) {
        if(messageUtil.isBanned(senderId)){
            throw new BannedSenderException();
        }
        FileType type = (FileType.getFileType(file)).orElseThrow(UnacceptableFileTypeException::new);

        // db에서 두사람 정보 들고오기
        Member sender = memberService.findByIdToEntity(senderId);

        SendResponse sendResponse = new SendResponse(false);

        int randomNumber = ThreadLocalRandom.current().nextInt(100);

        if(randomNumber < BOX_PROBABILITY){
            // db상에 boxcount도 증가시켜야 하고
            sender.updateBoxIncrease();
            sendResponse.setRandomBoxProvided(true);
        }

        // S3 저장
        Optional<String> result = s3Service.upload(file, type.getS3PathType());
        String s3Url = result.orElseThrow(S3UploadException::new);

        log.info("file send -> sender: {}", sender.getUserName());

        // message 전송
        messageRepository.save(
            Message.builder()
                .content(s3Url)
                .contentType(type.getContentType())
                .sender(sender)
                .receiver(null)
                .isReported(false)
                .readStatus(false)
                .parent(null)
                .isResponse(false)
                .responseStatus(false)
                .build()
        );

        return sendResponse;

    }

    @Override
    @Transactional
    public void responseFileMessage(MultipartFile file, Long senderId, Long messageId) {
        if(messageUtil.isBanned(senderId)){
            throw new BannedSenderException();
        }
        FileType type = (FileType.getFileType(file)).orElseThrow(UnacceptableFileTypeException::new);

        // message 정보 불러오기
        Optional<Message> targetMessage = messageRepository.findById(messageId);
        Message message = targetMessage.orElseThrow(MessageNotFoundException::new);
        if(message.getIsReported()){
            throw new ReportedMessageException();
        }

        message.updateResponseStatus(true);

        assert(message.getResponseStatus());



        // S3 저장
        Optional<String> result = s3Service.upload(file, type.getS3PathType());
        String s3Url = result.orElseThrow(S3UploadException::new);

        Message responseMessage = Message.builder()
            .content(s3Url)
            .contentType(type.getContentType())
            .sender(message.getReceiver())
            .receiver(message.getSender())
            .isReported(false)
            .readStatus(false)
            .parent(message)
            .isResponse(true)
            .responseStatus(null)
            .build();

        log.info("file response send -> sender: {}, receiver: {}", message.getReceiver().getUserName(), message.getSender().getUserName());

        // message 전송
        messageRepository.save(responseMessage);

        // fcm 발송
        if(message.getSender().getIsEnabled()){
            List<FcmNotification> fcmNotifications = message.getSender().getFcmNotifications();
            for(FcmNotification fcmNotification: fcmNotifications){
                if(fcmNotification == null) continue;
                if(fcmNotification.getMark()) continue;
                fcmUtil.sendMessage(fcmNotification.getFcmToken(), fcmNotification.getId(), FcmType.RESPONSE_MESSAGE);
            }
        }

    }

    @Override
    @Transactional
    public ResponseWithSuccess<List<MessageResponse>> getRecentMessages(Long firstId, Integer size, Long receiverId) {
        Member receiver = memberService.findByIdToEntity(receiverId);
        List<Message> messages = messageRepository.findRecentMessages(receiver.getId(), firstId, size);
        List<MessageResponse> body = messages.stream()
            .map(message -> {
                message.updateReadDate();
                message.updateReadStatus(true);
                return modelMapper.map(message, MessageResponse.class);
            })
            .collect(Collectors.toList());
        ResponseWithSuccess<List<MessageResponse>> response = new ResponseWithSuccess<>(body);
        if(messages.size() == EMPTY_MESSAGE){
            response.setSuccessType(SuccessType.STATUS_204);
        }
        return response;
    }

    @Override
    @Transactional
    public MessageResponse findMessage(Long messageId) {

        Message result = messageRepository.findById(messageId)
            .orElseThrow(MessageNotFoundException::new);

        return modelMapper.map(result, MessageResponse.class);
    }

    @Override
    @Transactional
    public ResponseWithSuccess<SliceMessageResponse> getOldMessages(Long lastId, Integer size, Long receiverId) {
        Member receiver = memberService.findByIdToEntity(receiverId);
        Slice<Message> sliceMessages = messageCustomRepository.findAllBySizeWithNotReported(lastId, size, receiver);
        sliceMessages.getContent().forEach(message -> {
            message.updateReadStatus(true);
            message.updateReadDate();
        });
        SliceMessageResponse body = SliceMessageResponse.to(sliceMessages, modelMapper);
        ResponseWithSuccess<SliceMessageResponse> response = new ResponseWithSuccess<>(body);
        if(sliceMessages.getContent().size() == EMPTY_MESSAGE){
            response.setSuccessType(SuccessType.STATUS_204);
        }
        return response;
    }

    @Override
    @Transactional
    public ResponseWithSuccess<SliceMessageResponse> getDailyOldMessages(Long lastId, Integer size) {
        Slice<Message> sliceMessages = messageCustomRepository.findAllBySizeWithNotReportedAndToday(lastId, size);
        SliceMessageResponse body = SliceMessageResponse.to(sliceMessages, modelMapper);
        ResponseWithSuccess<SliceMessageResponse> response = new ResponseWithSuccess<>(body);
        if(sliceMessages.getContent().size() == EMPTY_MESSAGE){
            response.setSuccessType(SuccessType.STATUS_204);
        }
        return response;
    }

    @Override
    @Transactional
    public ResponseWithSuccess<List<MessageResponse>> getDailyRecentMessages(Long firstId, Integer size) {
        LocalDate today = LocalDate.now();
        List<Message> messages = messageRepository.findRecentMessagesWithDaily(firstId, size, today.atStartOfDay(), today.atTime(23,59,59));
        List<MessageResponse> body = messages.stream()
            .map(message -> {
                return modelMapper.map(message, MessageResponse.class);
            })
            .collect(Collectors.toList());
        ResponseWithSuccess<List<MessageResponse>> response = new ResponseWithSuccess<>(body);
        if(messages.size() == EMPTY_MESSAGE){
            response.setSuccessType(SuccessType.STATUS_204);
        }
        return response;
    }

    @Override
    @Transactional
    public void updateReceiver(Long messageId, Long memberId){
        Message message = messageRepository.findByIdWithLock(messageId);
        Member member = memberService.findByIdToEntity(memberId);
        if(message.getIsResponse() || message.getReceiver() != null){
            throw new AlreadyAllocatedException();
        }
        message.updateReceiver(member);
    }

    @Override
    public ResponseWithSuccess<List<MessageResponse>> getNotReceivedRecentMessages(Long firstId, Integer size, Long memberId) {
        List<Message> messages = messageRepository.findNotReceivedRecentMessages(firstId, size, memberId);
        List<MessageResponse> body = messages.stream()
            .map(message -> {
                return modelMapper.map(message, MessageResponse.class);
            })
            .collect(Collectors.toList());
        ResponseWithSuccess<List<MessageResponse>> response = new ResponseWithSuccess<>(body);
        if(messages.size() == EMPTY_MESSAGE){
            response.setSuccessType(SuccessType.STATUS_204);
        }
        return response;
    }

    @Override
    public ResponseWithSuccess<SliceMessageResponse> getNotReceivedOldMessages(Long lastId, Integer size, Long memberId) {
        Slice<Message> sliceMessages = messageCustomRepository.findAllBySizeWithNotReceived(lastId, size, memberId);
        SliceMessageResponse body = SliceMessageResponse.to(sliceMessages, modelMapper);
        ResponseWithSuccess<SliceMessageResponse> response = new ResponseWithSuccess<>(body);
        if(sliceMessages.getContent().size() == EMPTY_MESSAGE){
            response.setSuccessType(SuccessType.STATUS_204);
        }
        return response;
    }


}
