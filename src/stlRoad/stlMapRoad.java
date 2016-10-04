package stlRoad;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class stlMapRoad {
	/**
	 * ���� |p1 p2| X |p1 p|
	 * @param p1 ����1
	 * @param p2 ����2
	 * @param p �жϵ�
	 * @return
	 */
	public static double getCross(LngLat p1,LngLat p2,LngLat p) {
		return (p2.lng - p1.lng) * (p.lat - p1.lat) -(p.lng - p1.lng) * (p2.lat - p1.lat);
	}
/**
 * 
 * @param a_lng ���뾭��
 * @param a_lat ����γ��
 * @param points ��γ�ȷ�Χ�����
 * @return 
 */
	public static boolean isPoinInPoly(LngLat lngLat ,LngLat [] points) {
		boolean flag=false;
		int iSum=0,icount;
		double d_lng1,d_lng2,d_lat1,d_lat2,d_lng;
		d_lat1=points[1].lat;
		d_lng1=points[1].lng;
		d_lat2=points[3].lat;
		d_lng2=points[3].lng;
		if (((lngLat.lat<=d_lat1)&&(lngLat.lat>=d_lat2))&&((lngLat.lng<=d_lng1)&&(lngLat.lng>=d_lng2))) {
			flag=true;
		}
//		for(int i=0;i<icount-1;i++)
//		{
//			if (i==icount-1) {
//				d_lng1=points[i].lng;
//				d_lat1=points[i].lat;
//				d_lng2=points[0].lng;
//				d_lat2=points[0].lat;
//			} else {
//				d_lng1=points[i].lng;
//				d_lat1=points[i].lat;
//				d_lng2=points[i+1].lng;
//				d_lat2=points[i+1].lat;
//			}
//			//��������ж�A���Ƿ��ڱߵ����˵��ˮƽƽ����֮�䣬��������н��㣬��ʼ�жϽ����Ƿ�����������
//            if (((lngLat.lat >= d_lat1) && (lngLat.lat < d_lat2)) || ((lngLat.lat >= d_lat2) && (lngLat.lat < d_lat1)))
//            {
//                if (Math.abs(d_lat1 - d_lat2) > 0)
//                {
//                    //�õ� A������������ߵĽ����x���꣺
//                	d_lng = d_lng1 - ((d_lng1 - d_lng2) * (d_lat1 - lngLat.lat)) / (d_lat1 - d_lat2);
//
//                    // ���������A����ࣨ˵������������ �ߵĽ��㣩����������ߵ�ȫ����������һ��
//                    if (d_lng < lngLat.lng)
//                        iSum++;
//                }
//            }
//            if (iSum % 2 != 0)
//            	flag=true;
//		}
		return flag;
	}

/**
 * 
 * @param str γ�ȣ�����
 * @return ��LngLat����γ��
 */	
	public static LngLat split(String str) {
		String [] strings=str.split(",");
		LngLat lngLat=new LngLat();
		lngLat.setLat(Double.valueOf(strings[0]));
		lngLat.setLng(Double.valueOf(strings[1]));
		return lngLat;
	}
	/**
	 * 
	 * @param lat stl��γ��
	 * @param lng stl�ľ���
	 * @return LngLat
	 */
	public static LngLat toLngLat (String lat,String lng) {
		LngLat lngLat=new LngLat();
		StringBuilder sb_lat=new StringBuilder(lat);
		StringBuilder sb_lng=new StringBuilder(lng);
		sb_lat.insert(2, '.');
		sb_lng.insert(3, '.');
		lngLat.setLat(Double.valueOf(sb_lat.toString()));
		lngLat.setLng(Double.valueOf(sb_lng.toString()));
		return lngLat;
	}
	/**
	 * 
	 * @param str ����ľ��Ȼ�γ�ȵ��ַ���
	 * @param i ����С�����λ��
	 * @return ��С����ľ�γ���ַ���
	 */
	private static String latLngStingAddPiont(String str,int i) {
		StringBuilder sb=new StringBuilder(str);
		sb.insert(i, ".");
		return sb.toString();
	}
	public static void getPiontInPoly(int i) {
		
		String sql1="SELECT rid,A,B,C,D FROM road";
		String sql2="select COUNT(UniqueID) AS icount from stl_p1_d01_copy";
		StringBuilder values=new StringBuilder();;
		StringBuilder sql4=null;
		StringBuilder sql5=null;
		int  icount=0,start=i,end=i+500000,istep=1;
		int rid;
		DBHelper db=new DBHelper();
		DBHelper db2=new DBHelper();
		
		try {
			db2.conn.setAutoCommit(false);
			ResultSet roadSet=db.pst.executeQuery(sql1);
			if(roadSet.next()){
			LngLat p_A,p_B,p_C,p_D;
			p_A=split(roadSet.getString("A"));
			p_B=split(roadSet.getString("B"));
			p_C=split(roadSet.getString("C"));
			p_D=split(roadSet.getString("D"));
			rid=roadSet.getInt("rid");
			LngLat [] points={p_A,p_B,p_C,p_D};
			ResultSet stlSet=null;
			ResultSet count=db.pst.executeQuery(sql2);
			if (count.next()) {
				icount=count.getInt(1);
			}
			   while (true) {
				sql5=new StringBuilder("SELECT UniqueID,DeviceID,Speed,Longitude,Latitude FROM stl_p1_d01_copy WHERE id BETWEEN ");
				sql5.append(String.valueOf(start)+" AND "+String.valueOf(end));
				System.out.println(sql5);
				stlSet=db.pst.executeQuery(sql5.toString());
				while (stlSet.next()) {
					if (isPoinInPoly(toLngLat(stlSet.getString("Latitude"),stlSet.getString("Longitude")), points)) {
						
						values.append("( '"+stlSet.getString(1)+"' ,");
						values.append(String.valueOf(rid)+",");
						values.append("'"+stlSet.getString(2)+"',");
						values.append(stlSet.getString(3)+",");
						values.append(latLngStingAddPiont(stlSet.getString(5), 2)+",");
						values.append(latLngStingAddPiont(stlSet.getString(4), 3)+"),");
						
					} 
					
				}
				sql4=new StringBuilder("insert into road_stl (uid,rid,deviceid,speed,lat,lng) values ");
				sql4.append(values.deleteCharAt(values.length()-1));
				System.out.println(sql4);
				values=new StringBuilder();
				db2.pst.addBatch(sql4.toString());
				db2.pst.executeBatch();
				db2.conn.commit();
				start=end+1;
				end+=500000;
				istep++;
				if (end>=icount) {
					break;
				}

			  }  
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			System.out.println("finish!");
			db.close();
			db2.close();
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		getPiontInPoly(1);
		
		DBHelper db=new DBHelper();
		try {
			for(int i=10;i<=31;i++)
			{
				String sql0="ALTER TABLE `stl_p1_d"+String.valueOf(i)+"MODIFY COLUMN `F1`  int(11) NOT NULL AUTO_INCREMENT AFTER `TimeStamp`,DROP PRIMARY KEY,ADD PRIMARY KEY (`F1`)";
				System.out.println(sql0);
				db.pst.executeUpdate(sql0);
			}
		} catch (SQLException  e) {
			// TODO: handle exception
			System.out.println(e.toString());
		}
		finally {
			db.close();
		}

	}

}
