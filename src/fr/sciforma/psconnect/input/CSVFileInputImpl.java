/*
 * © 2008 Sciforma. Tous droits réservés. 
 */
package fr.sciforma.psconnect.input;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

import fr.sciforma.psconnect.exception.BusinessException;
import fr.sciforma.psconnect.exception.TechnicalException;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import org.pmw.tinylog.Logger;

/**
 * implementation de la lecture d'un fichier CSV
 */
public class CSVFileInputImpl implements LineFileInput<String[]> {

    private Reader filename;

    public CSVFileInputImpl(String filename) throws BusinessException {
        try {
            //this.filename = new FileReader(filename);
            this.filename = new InputStreamReader(new FileInputStream(filename), "ISO-8859-15");
        } catch (FileNotFoundException e) {
            throw new BusinessException("Le fichier csv <" + filename
                    + "> n'a pas été trouvé");
        } catch (UnsupportedEncodingException ex) {
            Logger.error(ex);
        }
    }

    @SuppressWarnings("unchecked")
    public List<String[]> readAll() throws TechnicalException {
        List<String[]> list;
        Logger.debug("lecture du fichier " + this.filename);

        CSVReader reader = new CSVReader(this.filename, ';', '"', 1);

        try {
            list = reader.readAll();
        } catch (IOException e) {
            throw new TechnicalException(e);
        }
        try {
            reader.close();
        } catch (IOException e) {
            throw new TechnicalException(e);
        }

        Logger.debug("fin de la lecture du fichier csv " + this.filename);

        return list;
    }
}
