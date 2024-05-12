export interface IInquiry {
  content: IInquiryRes[];
  currentPage: number;
  size: number;
  first:boolean;
  last:boolean;
}

export interface IInquiryRes {
  id: number;
  subject: string;
  writerName: string;
  content: string;
  boardType: string;
  createDate: string;
  updateDate: string;
  comment: {
    id: number;
    commenterName:string;
    content: string;
    createDate: string;
    updateDate: string;
  };
}

export interface INotification {
  content: INotificationRes[];
  currentPage: number;
  size: number;
  first: boolean;
  last: boolean;
}

export interface INotificationRes {
  id: number;
  subject: string;
  writerName: string;
  content: string;
  boardType: string;
  createDate: string;
  updateDate: string;
  isCommented: boolean;
}