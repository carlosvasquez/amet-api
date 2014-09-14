/*
 * The MIT License
 *
 * Copyright 2014 Developers Dominicanos.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.devdom.api.amet;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.devdom.api.amet.exception.FormParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Carlos Vásquez Polanco
 */
public class FormParser {

    private final URL url;
    private final ArrayList<Form> formFields = new ArrayList<Form>();
    
    private FormParser(){
        throw new AssertionError();
    }
    
    public FormParser(String url) {
        try {
            this.url = new URL(url);
        } catch (MalformedURLException e) {
           throw new RuntimeException(e);
        }
    }
    
    private void readNodes(NodeList nodes){

        int len = nodes.getLength();

        for(int i = 0;i< len;i++){
            Node node = nodes.item(i);

            if(node.getNodeType() == Node.ELEMENT_NODE){
                 Element element = (Element) node;

                String nodeName = element.getNodeName();

                if(nodeName.equals(Form.Field.TYPE.getText())){

                    String id = element.getAttribute(Form.Field.ID.getText());
                    String name = element.getAttribute(Form.Field.NAME.getText());
                    String type = element.getAttribute(Form.Field.TYPE.getText());
                    String value = element.getAttribute(Form.Field.VALUE.getText());

                    formFields.add( new Form(id, name, value, type) ); // Agregar nuevo campos del formulario a la collección

                }

                NodeList newNodelist = element.getChildNodes();
                int elementsLen = newNodelist.getLength();
                
                if(elementsLen > 0){
                    readNodes(newNodelist); // Entrar a revisar otros nodos en búsqueda de elementos
                }
                
            }
        }
    }
    
    /**
     * Genera la colección de elementos a partir del listado recorriendo 
     * el documento HTML y extrae los campos del formulario existente.
     * 
     * @return
     * @throws FormParseException 
     */
    public ArrayList<Form> getForm() throws FormParseException{

        String out;
        Document doc = null;
        try{
            out = new Scanner(url.openStream(), "UTF-8").useDelimiter("\\A").next();
        }catch(Exception ex){
            throw new FormParseException(ex.getMessage(),ex);
        }
        
        try{
            doc = generateDocument(out);
            doc.getDocumentElement().normalize();
        }catch(Exception ex){
            throw new FormParseException(ex.getMessage(),ex);
        }

        NodeList nodes = doc.getElementsByTagName("form");
        readNodes(nodes);

        return formFields;
    }
    
    /**
     * 
     * @param xmlString
     * @return 
     */
    private Document generateDocument(String xmlString) throws FormParseException {
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance(); 
        DocumentBuilder builder; 
        
        try{
            builder = factory.newDocumentBuilder(); 
            Document doc = builder.parse( new InputSource( new StringReader( xmlString ) ) );
            return doc;
        } catch (IOException ex) { 
            throw new FormParseException(ex.getMessage(), ex);
        } catch (ParserConfigurationException ex) {
            throw new FormParseException(ex.getMessage(), ex);
        } catch (SAXException ex) {
            throw new FormParseException(ex.getMessage(), ex);
        }
    }
    
}
