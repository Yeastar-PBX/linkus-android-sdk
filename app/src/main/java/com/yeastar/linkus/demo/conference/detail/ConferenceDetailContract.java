package com.yeastar.linkus.demo.conference.detail;

import com.yeastar.linkus.demo.call.BaseView;
import com.yeastar.linkus.service.conference.vo.ConferenceVo;

/**
 * Created by ted on 17-4-25.
 */

public interface ConferenceDetailContract {

    interface Presenter {

        void startMeeting(ConferenceVo conferenceVo);

        void sortList(ConferenceVo conferenceVo);

    }

    interface View extends BaseView<Presenter> {

        void initUi();

        void showStartProgressDialog();

        void dismissProgressDialog();

    }
}
