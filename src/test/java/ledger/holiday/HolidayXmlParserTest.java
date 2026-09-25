package ledger.holiday;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HolidayXmlParserTest {

    private static String response(String resultCode, String items) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<response><header><resultCode>" + resultCode + "</resultCode><resultMsg>NORMAL SERVICE.</resultMsg></header>"
                + "<body>" + items + "<numOfRows>100</numOfRows><pageNo>1</pageNo><totalCount>3</totalCount></body></response>";
    }

    private static String item(String locdate, String name, String isHoliday) {
        return "<item><dateKind>01</dateKind><dateName>" + name + "</dateName><isHoliday>" + isHoliday
                + "</isHoliday><locdate>" + locdate + "</locdate><seq>1</seq></item>";
    }

    @Test
    void parsesHolidaysOnly() {
        String xml = response("00", "<items>" + item("20260101", "1월1일", "Y")
                + item("20260216", "설날", "Y") + item("20260101", "기념일", "N") + "</items>");
        assertEquals(List.of(
                        new HolidayXmlParser.Holiday(LocalDate.of(2026, 1, 1), "1월1일"),
                        new HolidayXmlParser.Holiday(LocalDate.of(2026, 2, 16), "설날")),
                HolidayXmlParser.parse(xml));
    }

    @Test
    void emptyItemsIsEmptyList() {
        assertTrue(HolidayXmlParser.parse(response("00", "<items/>")).isEmpty());
        assertTrue(HolidayXmlParser.parse(response("00", "<items></items>")).isEmpty());
    }

    @Test
    void errorResultCodeFails() {
        assertThrows(IllegalStateException.class, () -> HolidayXmlParser.parse(response("22", "<items/>")));
    }

    // 키 미등록 등 포털 게이트웨이 오류 응답
    @Test
    void portalErrorResponseFails() {
        String xml = "<OpenAPI_ServiceResponse><cmmMsgHeader><errMsg>SERVICE ERROR</errMsg>"
                + "<returnAuthMsg>SERVICE_KEY_IS_NOT_REGISTERED_ERROR</returnAuthMsg><returnReasonCode>30</returnReasonCode>"
                + "</cmmMsgHeader></OpenAPI_ServiceResponse>";
        assertThrows(IllegalStateException.class, () -> HolidayXmlParser.parse(xml));
    }

    // XXE: DOCTYPE 이 있으면 엔티티를 풀기 전에 거부한다
    @Test
    void doctypeIsRejected() {
        String xml = "<?xml version=\"1.0\"?><!DOCTYPE r [<!ENTITY x SYSTEM \"file:///c:/windows/win.ini\">]>"
                + "<response><header><resultCode>00</resultCode></header><body><items>"
                + item("20260101", "&x;", "Y") + "</items></body></response>";
        assertThrows(IllegalStateException.class, () -> HolidayXmlParser.parse(xml));
    }

    @Test
    void malformedFails() {
        assertThrows(IllegalStateException.class, () -> HolidayXmlParser.parse("Unauthorized"));
        assertThrows(IllegalStateException.class, () -> HolidayXmlParser.parse(response("00", "<items>" + item("2026-01-01", "x", "Y") + "</items>")));
    }
}
