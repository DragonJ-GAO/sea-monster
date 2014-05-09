package com.sea_monster.core.utils;

import android.os.Parcel;
import android.os.Parcelable;

import com.sea_monster.core.common.Const;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParcelUtils {

	public static void writeToParcel(Parcel out, String obj) {
		if (obj != null) {
			out.writeInt(Const.Parcel.EXIST_SEPARATOR);
			out.writeString(obj);
		} else {
			out.writeInt(Const.Parcel.NON_SEPARATOR);
		}
	}

    public static void writeToParcel(Parcel out, Long obj){
        if(obj!=null){
            out.writeInt(Const.Parcel.EXIST_SEPARATOR);
            out.writeLong(obj);
        }else {
            out.writeInt(Const.Parcel.NON_SEPARATOR);
        }
    }

    public static void writeToParcel(Parcel out, Integer obj){
        if(obj!=null){
            out.writeInt(Const.Parcel.EXIST_SEPARATOR);
            out.writeInt(obj);
        }else {
            out.writeInt(Const.Parcel.NON_SEPARATOR);
        }
    }

    public static void writeToParcel(Parcel out, Float obj){
        if(obj!=null){
            out.writeInt(Const.Parcel.EXIST_SEPARATOR);
            out.writeFloat(obj);
        }else {
            out.writeInt(Const.Parcel.NON_SEPARATOR);
        }
    }

	public static void writeToParcel(Parcel out, Map obj) {
		if (obj != null) {
			out.writeInt(Const.Parcel.EXIST_SEPARATOR);
			out.writeMap(obj);
		} else {
			out.writeInt(Const.Parcel.NON_SEPARATOR);
		}
	}

    public static void writeToParcel(Parcel out, Date obj) {
        if (obj != null) {
            out.writeInt(Const.Parcel.EXIST_SEPARATOR);
            out.writeLong(obj.getTime());
        } else {
            out.writeInt(Const.Parcel.NON_SEPARATOR);
        }
    }

    public static Float readFloatFromParcel(Parcel in) {
        int flag = in.readInt();
        if (flag == Const.Parcel.EXIST_SEPARATOR) {
            return in.readFloat();
        } else {
            return null;
        }
    }

    public static Date readDateFromParcel(Parcel in) {
        int flag = in.readInt();
        if (flag == Const.Parcel.EXIST_SEPARATOR) {
            return new Date(in.readLong());
        } else {
            return null;
        }
    }

    public static Integer readIntFromParcel(Parcel in) {
        int flag = in.readInt();
        if (flag == Const.Parcel.EXIST_SEPARATOR) {
            return in.readInt();
        } else {
            return null;
        }
    }

    public static Long readLongFromParcel(Parcel in) {
        int flag = in.readInt();
        if (flag == Const.Parcel.EXIST_SEPARATOR) {
            return in.readLong();
        } else {
            return null;
        }
    }

	public static String readFromParcel(Parcel in) {
		int flag = in.readInt();
		if (flag == Const.Parcel.EXIST_SEPARATOR) {
			return in.readString();
		} else {
			return null;
		}
	}

	public static Map readMapFromParcel(Parcel in) {
		int flag = in.readInt();
		if (flag == Const.Parcel.EXIST_SEPARATOR) {
			return in.readHashMap(HashMap.class.getClassLoader());
		} else {
			return null;
		}
	}

	public static <T extends Parcelable> T readFromParcel(Parcel in, Class<T> cls) {
		int flag = in.readInt();
		if (flag == Const.Parcel.EXIST_SEPARATOR) {
			return in.readParcelable(cls.getClassLoader());
		} else {
			return null;
		}
	}

	public static <T extends Parcelable> void writeToParcel(Parcel out, T model) {
		if (model != null) {
			out.writeInt(Const.Parcel.EXIST_SEPARATOR);
			out.writeParcelable(model, 0);
		} else {
			out.writeInt(Const.Parcel.NON_SEPARATOR);
		}
	}

	public static <T extends List<?>> void writeToParcel(Parcel out, T model) {
		if (model != null) {
			out.writeInt(Const.Parcel.EXIST_SEPARATOR);
			out.writeList(model);
		} else {
			out.writeInt(Const.Parcel.NON_SEPARATOR);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends Object> ArrayList<T> readListFromParcel(Parcel in, Class<T> cls) {
		int flag = in.readInt();
		if (flag == Const.Parcel.EXIST_SEPARATOR) {
			return in.readArrayList(cls.getClassLoader());
		} else {
			return null;
		}
	}

	public static void writeListToParcel(Parcel out, List<?> collection) {
		if (collection != null) {
			out.writeInt(Const.Parcel.EXIST_SEPARATOR);
			out.writeList(collection);
		} else {
			out.writeInt(Const.Parcel.NON_SEPARATOR);
		}
	}

	public static <T extends Parcelable> T bytesToParcelable(byte[] data, Class<T> cls) {
		if (data == null || data.length == 0)
			return null;

		Parcel in = Parcel.obtain();
		in.unmarshall(data, 0, data.length);
		in.setDataPosition(0);

		T t;
		t = readFromParcel(in, cls);
		in.recycle();
		return t;
	}

	public static byte[] parcelableToByte(Parcelable model) {
		if (model == null)
			return null;
		Parcel parcel = Parcel.obtain();		
		writeToParcel(parcel, model);
		return parcel.marshall();
	}

	public static <T extends Parcelable> List<T> bytesToParcelableList(byte[] data, Class<T> cls) {
		if (data == null || data.length == 0)
			return null;

		Parcel in = Parcel.obtain();
		in.unmarshall(data, 0, data.length);
		in.setDataPosition(0);

		List<T> t;
		t = readListFromParcel(in, cls);
		in.recycle();
		return t;
	}

	public static byte[] parcelableListToByte(List<? extends Parcelable> list) {
		if (list == null)
			return null;
		Parcel parcel = Parcel.obtain();
		
		writeListToParcel(parcel, list);
		return parcel.marshall();
	}

}
