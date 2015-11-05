package com.metova.finiteflow;

import android.content.Context;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class PersistenceUtil {

    private static final String TAG = PersistenceUtil.class.getSimpleName();
    private static final String SUFFIX = "_flow";

    enum PERSIST_TYPE {
        TYPE_DISK,
        TYPE_SHARED_PREFS
    }

    /**
     * Persist an instance of a flow
     * @param persistType How to persist the flow
     * @param flow The flow to persist
     * @return If the persistence was successful or not
     */
    public static boolean persistFlow(Context context, PERSIST_TYPE persistType, FiniteFlow flow) {

        switch (persistType) {

            case TYPE_DISK:

                return persistDisk(context, flow);

            case TYPE_SHARED_PREFS:

                return persistSharedPrefs(context, flow);
        }

        return false;
    }

    public static FiniteFlow readFlow(Context context, PERSIST_TYPE persistType, String flowIdentifier) {

        switch (persistType) {

            case TYPE_DISK:

                return readDisk(context, flowIdentifier);

            case TYPE_SHARED_PREFS:

                return readSharedPrefs(context, flowIdentifier);
        }

        return null;
    }

    private static boolean persistDisk(Context context, FiniteFlow flow) {

        FileOutputStream fos = null;
        try {
            fos = context.openFileOutput(flow.getIdentifier() + SUFFIX, Context.MODE_PRIVATE);
            fos.write(serialize(flow));
            fos.close();
            return true;
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Exception in persistDisk", e);
        } catch (IOException e) {
            Log.e(TAG, "Exception in persistDisk", e);
        }

        return false;
    }

    private static boolean persistSharedPrefs(Context context, FiniteFlow flow) {

        // TODO: Implement this (need a way to serialize / deserialize via the SharedPrefs)
        Log.w(TAG, "persistSharedPrefs not implemented yet!");
        return false;
    }

    private static FiniteFlow readDisk(Context context, String flowIdentifier) {

        FileInputStream fis = null;
        FiniteFlow finiteFlow = null;

        try {
            fis = context.openFileInput(flowIdentifier + SUFFIX);
            int size = (int) fis.getChannel().size();
            byte[] bytes = new byte[size];
            byte tmpBuff[] = new byte[size];

            int read = fis.read(bytes, 0, size);
            if (read < size) {
                int remain = size - read;
                while (remain > 0) {
                    read = fis.read(tmpBuff, 0, remain);
                    System.arraycopy(tmpBuff, 0, bytes, size - remain, read);
                    remain -= read;
                }
            }

            finiteFlow = deserialize(bytes);
        }
        catch (FileNotFoundException e) {
            Log.e(TAG, "Exception in readDisk", e);
        }
        catch (IOException e) {
            Log.e(TAG, "Exception in readDisk", e);
        }
        catch (ClassNotFoundException e) {
            Log.e(TAG, "Exception in readDisk", e);
        } finally {
            if(fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    Log.e(TAG, "Exception in readDisk", e);
                }
            }
        }

        return finiteFlow;
    }

    private static FiniteFlow readSharedPrefs(Context context, String flowIdentifier) {

        // TODO: Implement this (need a way to serialize / deserialize via the SharedPrefs)
        Log.w(TAG, "readSharedPrefs not implemented yet!");
        return null;
    }


    // region Serialization
    private static byte[] serialize(FiniteFlow flow) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(flow);
        return out.toByteArray();
    }
    private static FiniteFlow deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return (FiniteFlow) is.readObject();
    }
    // endregion
}
