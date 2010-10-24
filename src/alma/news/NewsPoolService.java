package alma.news;

import javax.jms.Message;
import javax.jms.MessageFormatException;
import javax.jms.MessageListener;

import org.apache.derby.iapi.util.StringUtil;
import org.exolab.jms.message.ObjectMessageImpl;

import alma.common.models.StatefulBean;
import alma.common.models.vo.NewsVO;
import alma.common.services.AsyncReceiver;
import alma.common.services.QueueMessageSender;
import antlr.StringUtils;


/**
 * NewsPool Service.
 * 
 * The news pool produces/retrieves some text, which belong to specific 
 * categories (it is useless to model further its treatment), and send it to a
 * publish/subscribe component. The latter is accessed by a set of editors that
 * treat the news dispatch for which they are interested in, and thus, they have
 * subscribe to. The pool also sends the news dispatch <em>id</em> to the
 * editor-in-chief, in a <em>secure</em> and <em>acknowledged</em> way.
 * 
 * @author Sylvain Lecoy
 * @author Fréderic Dumont
 *
 */
public class NewsPoolService extends StatefulBean implements MessageListener {

	public static void main(String[] args) {
		NewsPoolService newsPool = new NewsPoolService(); // Créer le Service de Nouvelles.
	}

	private static int uniqueId = 0;
	private QueueMessageSender idSender = null;
	private QueueMessageSender newsSender = null;

	public NewsPoolService() {
		//Créer un News Sender vers la destination "newsToEditQueue".
		newsSender = new QueueMessageSender("newsToEditQueue");

		//Créer un Id Sender vers la destination "newsToValidateQueue".
		idSender = new QueueMessageSender("newsToValidateQueue");

		//Configure un AsyncReceiver anonyme et lui passe en référence l'objet.
		receiver = new AsyncReceiver("newsPoolQueue", this);

		start();
		System.out.println("Service de NewsPool lancé.");
	}

	//Fonction déclenchée lors de la réception d'une nouvelle sur la queue "newsPool".
	public void onMessage(Message m) {
		try {
			if (m instanceof ObjectMessageImpl) {
				ObjectMessageImpl message = (ObjectMessageImpl)m;
				NewsVO news = (NewsVO) message.getObject();

				news.id = uniqueId++; //Attribut une id unique au système.
			    
				System.out.println("Nouvelle reçue: " + news);
				
				idSender.sendTextMessage(String.valueOf(news.id));
				newsSender.sendObjectMessage(news);
			} else {
				System.out.println("Message wrong type: " + 
						m.getClass().getName());
			}
		} catch (Throwable t) {
			System.out.println("Exception in onMessage():" + t.getMessage());
		}
	}

}