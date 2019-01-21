/*
 * © 2008 Sciforma. Tous droits réservés. 
 */
package fr.sciforma.psconnect.manager.filter;

import com.sciforma.psnext.api.FieldAccessor;
import com.sciforma.psnext.api.PSException;

public interface Filter<T extends FieldAccessor> {

	abstract boolean filter(T fieldAccessor) throws PSException;

}
