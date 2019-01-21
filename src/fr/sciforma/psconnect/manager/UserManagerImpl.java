/*
 * © 2008 Sciforma. Tous droits réservés. 
 */
package fr.sciforma.psconnect.manager;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.sciforma.psnext.api.DataFormatException;
import com.sciforma.psnext.api.PSException;
import com.sciforma.psnext.api.Session;
import com.sciforma.psnext.api.User;

import fr.sciforma.psconnect.exception.BusinessException;
import fr.sciforma.psconnect.exception.TechnicalException;
import org.pmw.tinylog.Logger;

public class UserManagerImpl implements UserManager {

        private Session session;

	private boolean hasUseCacheUser = true;

	private Map<String, User> _mapUser = new HashMap<String, User>();

	public UserManagerImpl(Session session) {
		this.session = session;
	}

	public User findUserById(final String id) {
		if (hasUseCacheUser && _mapUser.get(id) != null) {
			return _mapUser.get(id);
		}

		List<User> findAndCacheByFilter = findAndCacheByFilter(
				new UserFilter() {
					public boolean filter(User user) throws PSException {
						return id != null
								&& id.equalsIgnoreCase(user.getStringField("Login ID"));
					}
				}, true);

		return findAndCacheByFilter.isEmpty() ? null : findAndCacheByFilter
				.iterator().next();
	}

	public List<User> findUserByCriteria(UserFilter userFilter) {
		return findAndCacheByFilter(userFilter, false);
	}

	@SuppressWarnings("unchecked")
	List<User> findAndCacheByFilter(UserFilter userFilter, boolean firstStop) {
		List<User> users = new LinkedList<User>();
		try {
			for (User user : (List<User>) this.session.getUserList()) {
				if (userFilter != null && userFilter.filter(user)) {
					users.add(user);
					if (firstStop) {
						return users;
					}
				}
				if (hasUseCacheUser) {
					putInCache(user.getStringField("Login ID"), user);
				}
			}
			return users;
		} catch (DataFormatException e) {
			String message = "Il y a eu un problème de format de données lors du parcours des utilisateurs.";
			Logger.error(message, e);
			throw new TechnicalException(e, message);
		} catch (PSException e) {
			String message = "Il y a eu un problème technique lors du parcours des utilisateurs.";
			Logger.error(message, e);
			throw new TechnicalException(e, message);
		}
	}

	private void putInCache(String ID, User user) {
		this._mapUser.put(ID.toLowerCase(), user);
	}
}
