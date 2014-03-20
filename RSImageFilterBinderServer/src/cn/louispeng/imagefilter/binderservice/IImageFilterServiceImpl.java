/**
 * @author pengluyu
 *
 * IImageFilterServiceImpl.java
 * 11:19:51 PM 2014
 */

package cn.louispeng.imagefilter.binderservice;

import cn.louispeng.imagefilter.bindercommon.IImageFilterService;
import cn.louispeng.imagefilter.bindercommon.IImageFilterServiceResponseListener;

import android.os.ParcelFileDescriptor;
import android.os.RemoteException;

/**
 * @author pengluyu
 */
public class IImageFilterServiceImpl extends IImageFilterService.Stub {

    /*
     * (non-Javadoc)
     * @see
     * cn.louispeng.imagefilter.bindercommon.IImageFilterService#filter(int,
     * android.os.ParcelFileDescriptor,
     * cn.louispeng.imagefilter.bindercommon.IImageFilterServiceResponseListener
     * )
     */
    @Override
    public void filter(int effectID, ParcelFileDescriptor fd, IImageFilterServiceResponseListener listener)
            throws RemoteException {
        
    }

}
