package ox.softeng.burst.util;

import java.util.Arrays;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import ox.softeng.burst.domain.*;

  
public class HibernateUtil {
  
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            // Create the SessionFactory from hibernate.cfg.xml
                Configuration cfg=new Configuration();
            cfg.configure("hibernate.cfg.xml");
            List<Class<? extends Object>> domainClasses = buildClassList();
            for(Class<?> cl : domainClasses)
            {
                cfg.addAnnotatedClass(cl);
                
            }

            ServiceRegistry serviceRegistry=new StandardServiceRegistryBuilder().applySettings(cfg.getProperties()).build();

            return cfg.buildSessionFactory(serviceRegistry);
            
        } catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }
  
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
  
    public static void shutdown() {
        // Close caches and connection pools
        getSessionFactory().close();
    }
    
    private static List<Class<? extends Object>> buildClassList()
    {
                return Arrays.asList(
                        Message.class,
                		Severity.class,
                		Subscription.class,
                		Topic.class,
                		User.class
                );
    }
  
}