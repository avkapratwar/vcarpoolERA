package com.virtusa.vcarpoool.dao;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.persistence.Temporal;

import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.virtusa.vcarpoool.model.Employee;
import com.virtusa.vcarpoool.model.FindRide;
import com.virtusa.vcarpoool.model.Provide;

import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;

/**
 * @author amruthar
 *
 */
@Repository
public class VcarpoolDaoImpl implements VcarpoolDaoIface {
	private static final String GETPOOLDETAILS = "FROM Provide WHERE employeeid = :employee_id and status='Active'";
	private static final String GETEMPLOYEEDETAILS="FROM Employee WHERE employeeId=:employee_id and password=:pass";
	private static final String GENERATEPOOLID="FROM Provide";
	private static final String CHECKEMPLOYEE="FROM Employee WHERE employeeid=:employee_id and sq=:sq AND sa=:sa";
	private static final String UPDATEPASS="update Employee set password=:pass where employeeid=:empid" ;
	private static final String NUMBEROFRIDES = "from Provide where source = :source  and destination = :destination and noofseats >= :noOfSeats "
			+ "and lower(status) = 'active' and currentdate = :curr";
	private static final String UPDATESEATS = "update Provide set noOfSeats=:no where poolId=:poolid";
	private static final String EMAILIDDETAILS = "from Employee where employeeid =:empid";
	//private static final String SAVERIDE = "Select max(bookingId) from FindRide";
	private static final String BOOKINGID = "from FindRide";
	private static final String EMPID = "employee_id";
	boolean returnstmt = true;
//	public VcarpoolDaoImpl() {
//		new VcarpoolDaoImpl();
//	}
	
	
	
	
	@Autowired
	SessionFactory sessionFactory;

	public VcarpoolDaoImpl(SessionFactory sessionFactory) {
	super();
	this.sessionFactory = sessionFactory;
}
/**
	 * @return
	 */
	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	/**
	 * @param sessionFactory
	 */
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Generated(GenerationTime.ALWAYS)
	@Temporal(javax.persistence.TemporalType.DATE)
	Date dateoperation = new java.sql.Date(new java.util.Date().getTime());
	
	/**
	 *
	 */
	@Transactional
	public List<Provide> numberOfRides(String src, String des, int noseats) {
		Query q = this.getSessionFactory().getCurrentSession().createQuery(NUMBEROFRIDES);
		q.setParameter("source", src);
		q.setParameter("destination", des);
		q.setParameter("noOfSeats", noseats);
		Date d = new Date();
		String pattern = "dd-MMM-yy";
		SimpleDateFormat dt = new SimpleDateFormat(pattern);
		String d1 = dt.format(d);
		q.setParameter("curr", d1);
		return q.list();
	}

	/**
	 *
	 */
	@Transactional
	public int updateSeatsService(int poolid, int noseats,int num) {
		//int seats = 0;
		int no = 0;
		no= noseats-num;
		try {
			Query q = this.getSessionFactory().getCurrentSession()
					.createQuery(UPDATESEATS);
			q.setParameter("no", no);
			q.setParameter("poolid", poolid);
			return q.executeUpdate();
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			return 0;
		}
	}
	
	
	
	
	static class Authent extends Authenticator{
		
	}
	
	
	/**
	 *
	 */
	public Boolean sendEmail(String toMailRider, String toMailProvider, int bookId, Provide provider,int numberofseats,
			long contactnoProvider, long contactnoRider) {
		String from = "vcarpool2019@gmail.com";
		final String userName = "vcarpool2019@gmail.com";
		final String pass = "virtusaproject@123";
System.out.println("fsdfds");
		// sending email through relay.jangosmtp.net
		String host = "smtp.gmail.com";
		Properties props = new Properties();
		props.put(" mail.smtp.connectiontimeout", 100000);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.ssl.trust", "*");
		props.setProperty("mail.smtp.host", host);
		props.setProperty("mail.smtp.port", "587");

		
		
		
		
		// Get the Session object.
		javax.mail.Session mailSession = javax.mail.Session.getInstance(props, new Authent() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				System.out.println("valid");
				return new PasswordAuthentication(userName, pass);
			}
		});


		
			try {
				// Create a default MimeMessage object.
				Message message = new MimeMessage(mailSession);
				Message messageProvider = new MimeMessage(mailSession);

				// Set From: header field of the header.
				message.setFrom(new InternetAddress(from));

				// Set To: header field of the header.
				// send msg to rider
				message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toMailRider));

				// send mail to provider
				messageProvider.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toMailProvider));

				// Set Subject: header field
				message.setSubject("VCarPool Booking");
				messageProvider.setSubject("VCarPool Book, Pool details");

				// Now set the actual message
				// send details of provider to rider
				message.setText(" Yay! Your Ride Has Been Booked With VCarPool....  \n \n Booking ID:" + bookId
						+ "\n Pool ID: " + provider.getPoolId() + "\n From:" + provider.getSource() + "\n To:"
						+ provider.getDestination() + "\n Number of Seats:" + numberofseats + " \n Provider Contact:"
						+ contactnoProvider);

				// send details of rider to provider
				messageProvider.setText(" Yay! Your Booking With VCarPool is done \n \n Booking ID:" + bookId
						+ "\n Pool ID: " + provider.getPoolId() + "\n From:" + provider.getSource() + "\n To:"
						+ provider.getDestination() + "\n Number of Seats:" + numberofseats + " \n Rider Contact:"
						+ contactnoRider);

				// Send message
				Transport.send(message);
				Transport.send(messageProvider);
				return returnstmt;
			} catch (AddressException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				System.out.println("faileddd");
				e.printStackTrace();
			}
			return true;

		
	}

	/**
	 *
	 */
	@Transactional
	public Employee emailIdDetails(int empid) {
	Query q=this.getSessionFactory().getCurrentSession().createQuery(EMAILIDDETAILS);
	q.setParameter("empid", empid);
	
	return  (Employee) q.list().get(0);
	}
	
	
	

	/**
	 *
	 */
	@Transactional
	public Boolean addEmployee(Employee employee) {
			this.sessionFactory.getCurrentSession().saveOrUpdate(employee);
			return returnstmt;
			
	}

	/**
	 *
	 */
	@Transactional
	public Employee authenticateEmployee(int employeeid, String pass) {
		Query query = this.sessionFactory.getCurrentSession().createQuery(GETEMPLOYEEDETAILS);
		query.setParameter(EMPID, employeeid);
		query.setParameter("pass", pass);
	List<Employee> results = query.list();

		if(results.isEmpty()) {
			return null;
		}
			return results.get(0);
	}

	/**
	 *
	 */
	@Transactional
	public Provide getPoolDetails(int employeeid) {
		Query query = this.sessionFactory.getCurrentSession().createQuery(GETPOOLDETAILS);
		query.setParameter(EMPID,employeeid );
		List<Provide> results = query.list();
		
		if(results.isEmpty()) {
			return null;
		}
			return results.get(0);
	}
	/**
	 *
	 */
	@Transactional
	public int saveFind(FindRide find) {
		Query q= this.getSessionFactory().getCurrentSession().createQuery(BOOKINGID);
		
//		int n=0;
//		  List<Integer> n2=q.list();
//		  if(n2.isEmpty()) {
//			n=1;  
//		  }
//		  else
//		  {
//		   n=n2.get(0);
//		  n=n+1;
//		  }
//		  
//		  find.setBookingId(n); 
//		  
//		  int n1=(Integer)this.getSessionFactory().getCurrentSession().save(find); 
//		  return n;
		int n=0;
		List<FindRide> find1 = q.list();
		if(find1.isEmpty()) {
			n= 1;
		}else {
			n= find1.get(find1.size()-1).getBookingId()+1;
			
			
		}
		find.setBookingId(n);
		 return (Integer)this.getSessionFactory().getCurrentSession().save(find); 
	}
		
	/**
	 *
	 */
	@Transactional
	public int autoGeneratePoolId() {
				Query query = this.sessionFactory.getCurrentSession().createQuery(GENERATEPOOLID);
				List<Provide> results = query.list();
				if (results.isEmpty()) {
					return 1;
				}
				Provide provide = results.get(results.size() - 1);
				return provide.getPoolId() + 1;
	}

	/**
	 *
	 */
	@Transactional
	public Boolean createPool(Provide provider) {
			this.sessionFactory.getCurrentSession().saveOrUpdate(provider);
			return returnstmt;
		
	}

	/**
	 *
	 */
	@Transactional
	public Employee checkEmployee(Employee employee) {
		Query query = this.sessionFactory.getCurrentSession().createQuery(CHECKEMPLOYEE);
		query.setParameter(EMPID,employee.getEmployeeId());
		query.setParameter("sq", employee.getSecurityQuestion());
		query.setParameter("sa", employee.getSecurityAnswer());
		List<Employee> results = query.list();
		if(results.isEmpty()) {
			return null;
		}
		return results.get(0);
	}

	/**
	 *
	 */
	@Transactional
	public Boolean updatePass(int employeeid, String pass) {
		Query query = this.sessionFactory.getCurrentSession().createQuery(UPDATEPASS);
		query.setParameter("pass", pass);
		query.setParameter("empid",employeeid);
		query.executeUpdate();
		return returnstmt;
	}

}
