package operationXML;


import org.xml.sax.SAXException;
import parallelzation.graph.SWFGraph;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;

/**
 * @author LiTao
 * @date 2019/4/30 17:42
 */
public class ParseSWFXML {


    public static SWFGraph getSWF(String filePath){
        try{
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            File f = new File(filePath);
            SaxHandler dh = new SaxHandler();
            parser.parse(f, dh);
            //获取并解析XML工作流
            SWFGraph swfGraph=dh.InitGraph();
            return swfGraph;

        }catch (SAXException | ParserConfigurationException | IOException e){
            e.printStackTrace();
            return null;
        }

    }
}
