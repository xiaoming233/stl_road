package stlRoad;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class stlMapRoad {
	/**
	 * 计算 |p1 p2| X |p1 p|
	 * @param p1 顶点1
	 * @param p2 顶点2
	 * @param p 判断点
	 * @return
	 */
	public static double getCross(LngLat p1,LngLat p2,LngLat p) {
		return (p2.lng - p1.lng) * (p.lat - p1.lat) -(p.lng - p1.lng) * (p2.lat - p1.lat);
	}
/**
 * 
 * @param a_lng 输入经度
 * @param a_lat 输入纬度
 * @param points 经纬度范围多边形
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
//			//以下语句判断A点是否在边的两端点的水平平行线之间，在则可能有交点，开始判断交点是否在左射线上
//            if (((lngLat.lat >= d_lat1) && (lngLat.lat < d_lat2)) || ((lngLat.lat >= d_lat2) && (lngLat.lat < d_lat1)))
//            {
//                if (Math.abs(d_lat1 - d_lat2) > 0)
//                {
//                    //得到 A点向左射线与边的交点的x坐标：
//                	d_lng = d_lng1 - ((d_lng1 - d_lng2) * (d_lat1 - lngLat.lat)) / (d_lat1 - d_lat2);
//
//                    // 如果交点在A点左侧（说明是做射线与 边的交点），则射线与边的全部交点数加一：
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
 * @param str 纬度，经度
 * @return （LngLat）经纬度
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
	 * @param lat stl的纬度
	 * @param lng stl的经度
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
	 * @param str 输入的经度或纬度的字符串
	 * @param i 插入小数点的位置
	 * @return 带小数点的经纬度字符串
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
