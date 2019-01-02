 package com.hrw.downlibrary.service;

import android.support.annotation.IntDef;

import static com.hrw.downlibrary.service.ServiceActionType.DELETE;
import static com.hrw.downlibrary.service.ServiceActionType.DELETE_ALL;
import static com.hrw.downlibrary.service.ServiceActionType.RESUME;
import static com.hrw.downlibrary.service.ServiceActionType.START;
import static com.hrw.downlibrary.service.ServiceActionType.STOP;
import static com.hrw.downlibrary.service.ServiceActionType.STOP_ALL;

 /**
 * @version 1.0.0
 * @author:hrw
 * @date:2018/12/29 10:59
 * @desc:
 */

@IntDef({
        START,STOP,STOP_ALL,RESUME,DELETE,DELETE_ALL
})
public @interface ServiceActionType {
    int START       =0;
    int STOP        =1;
    int STOP_ALL    =2;
    int RESUME      =3;
    int DELETE      =4;
    int DELETE_ALL  =5;
}
