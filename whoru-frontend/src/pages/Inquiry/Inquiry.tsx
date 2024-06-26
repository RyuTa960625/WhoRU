import Header, { IHeaderInfo } from '@/components/@common/Header'
import NavigationBar from '@/components/@common/NavigationBar'
import InquiryManage from '@/components/Inquiry/InquiryManage'
import Backspace from '@/assets/@common/Backspace.png'

const Inquiry = () => {
  const info: IHeaderInfo = {
    left_1: null,
    left_2: <img src={Backspace} alt="" />,
    center: '문의사항',
    right: null
  }

	return (
		<div>
			<div>
				<Header info={info} />
			</div>
			<div className="pt-10">
				<InquiryManage />
			</div>
			<div>
				<NavigationBar />
			</div>
		</div>
	)
}

export default Inquiry
