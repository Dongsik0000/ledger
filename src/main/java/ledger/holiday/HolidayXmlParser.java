package ledger.holiday;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

// 공공데이터포털 특일정보(getRestDeInfo) XML 응답 → 공휴일 목록(isHoliday=Y 만).
// 외부에서 받은 XML 이므로 DTD·외부 엔티티를 막고(XXE 방지) 해석한다. 실패는 IllegalStateException.
public final class HolidayXmlParser {

    public record Holiday(LocalDate date, String name) {
    }

    private static final DateTimeFormatter LOCDATE = DateTimeFormatter.BASIC_ISO_DATE; // yyyyMMdd

    private HolidayXmlParser() {
    }

    public static List<Holiday> parse(String xml) {
        Document doc = read(xml);
        Element root = doc.getDocumentElement();
        if (!"response".equals(root.getTagName())) {
            // 키 미등록 등 포털 게이트웨이 오류(OpenAPI_ServiceResponse)
            throw new IllegalStateException("공휴일 API 오류 응답: " + text(root, "returnAuthMsg"));
        }
        String resultCode = text(root, "resultCode");
        if (!"00".equals(resultCode)) {
            throw new IllegalStateException("공휴일 API 결과 코드: " + resultCode);
        }

        List<Holiday> holidays = new ArrayList<>();
        NodeList items = root.getElementsByTagName("item");
        for (int i = 0; i < items.getLength(); i++) {
            Element item = (Element) items.item(i);
            if (!"Y".equals(text(item, "isHoliday"))) {
                continue;
            }
            try {
                holidays.add(new Holiday(LocalDate.parse(text(item, "locdate"), LOCDATE), text(item, "dateName")));
            } catch (RuntimeException e) {
                throw new IllegalStateException("공휴일 날짜 형식 오류", e);
            }
        }
        return holidays;
    }

    private static Document read(String xml) {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            f.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            f.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            f.setXIncludeAware(false);
            f.setExpandEntityReferences(false);
            DocumentBuilder builder = f.newDocumentBuilder();
            builder.setErrorHandler(new DefaultHandler());  // 오류를 stderr 에 찍지 않고 예외로만 전달
            return builder.parse(new InputSource(new StringReader(xml)));
        } catch (Exception e) {
            throw new IllegalStateException("공휴일 API 응답을 해석할 수 없음", e);
        }
    }

    private static String text(Element parent, String tag) {
        NodeList nodes = parent.getElementsByTagName(tag);
        return nodes.getLength() == 0 ? "" : nodes.item(0).getTextContent().trim();
    }
}
