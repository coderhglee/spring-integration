package com.coderhglee.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import javax.swing.text.*;
import javax.swing.text.rtf.RTFEditorKit;
import java.awt.*;
import java.io.*;
import java.util.Arrays;

@SpringBootTest
class ApiApplicationTests {

    @Value("${working.path}")
    private String WORKING_PATH;

    @Test
    void contextLoads() throws IOException, BadLocationException, FontFormatException {

        String inputText = "서울 강남구 개포주공아파트 4단지가 GS건설의 자이(Xi)와 만나 ‘개포 프레지던스 자이’로 탈바꿈한다.  자이는 국내 최고의 프리미엄 아파트 브랜드라는 평가를 받는다. 다양한 조사 결과가 이를 뒷받침한다.  \n" +
                "부동산정보업체 부동산114가 실시한 ‘2019년 베스트 아파트 브랜드’ 설문조사에서 자이는 브랜드 최초 상기도(27.3%), 선호도(24.8%), 정비사업 선호 브랜드(28.8%) 등의 평가항목에서 1위를 하고, 종합 1위에도 올랐다. 3년 연속으로 베스트 아파트 순위 1위를 차지했다.  \n" +
                "자이는 브랜드 가치 평가회사인 브랜드스탁이 발표한 올해 ‘대한민국 100대 브랜드’에서도 아파트 부문 1위에 올랐다. 부동산포털 닥터아파트가 실시한 2018년 아파트 브랜드 파워 설문조사에서는 3년 연속으로 1위를 기록했다.  \n" +
                "브랜드가 주택 가격에도 큰 영향을 미치는 ‘브랜드 아파트 시대’를 맞아, 부동산시장에서 자이의 위상은 더욱 높아질 전망이다.  \n" +
                " \n" +
                "◇출발부터 ‘프리미엄’ 앞세워 성공한 자이 \n" +
                "지난 2002년 첫 선을 보인 자이는 국내 아파트시장에서 상대적으로 후발주자에 속한다. 그럼에도 자이는 GS건설을 대표하는 고급 아파트의 대명사로 자리잡게 만든 성공적인 브랜드 마케팅 사례로 꼽힌다. 최근 국내 건설사들이 한층 깐깐해진 소비자들의 눈높이에 맞추기 위해 일반 아파트 브랜드와 구분되는 프리미엄 브랜드를 별도로 선보이고 있지만, GS건설은 처음부터 고급화 전략을 선택했다. 브랜드 출발 단계부터 ‘프리미엄 아파트’라는 브랜드 정체성을 갖고 출범해, 현재도 자이 브랜드 단독 체제를 고수하고 있다.  \n" +
                "자이의 브랜드 구상도 아주 혁신적인 시도였다는 평가를 받았다. 아파트를 단순한 주거공간이 아닌 소비자의 삶의 방식을 구현하는 공간으로 제시했기 때문이다. 2000년대 초반만 해도 건설사들이 건설사 이미지와 연관성이나 브랜드의 가치관과 크게 관련되지 않는 영문 이름으로 아파트 브랜드를 짓던 분위기였지만, GS건설은 아파트 브랜드와 소비자들이 추구하는 라이프 스타일을 결합하는 방식을 택했다. 자이는 ‘특별한 지성(eXtra Intelligence)’의 약자로, 자이에 거주하는 소비자들이 특별하고 지적인 삶의 방식을 구현하는 공간이란 뜻을 담고 있다. 이 같은 브랜드 가치를 기본으로 GS건설은 국내 건설업계에서는 처음으로 홈 네트워크 시스템을 도입하는 등 자이를 고품격 아파트 브랜드로 각인시키기 위해 노력했다.  \n" +
                "건설업계에서 최초로 주민들을 위한 모임 공간 등 공용시설을 아우르는 ‘커뮤니티’ 콘셉트를 아파트에 도입한 것도 GS건설이다. GS건설은 자이의 주거시스템과 디자인 등에 소비자들의 취향과 기호를 빠르게 포착해 접목하면서 주거문화를 선도하는 브랜드로 육성했다. 이 같은 자신감으로 제작한 광고 문구가 바로 ‘메이드 인 자이(Made in Xi)다.  \n" +
                " \n" +
                "◇차별화된 소비자 중심 서비스 \n" +
                "‘자이안센터(Xian Center)’는 자이의 차별화된 소비자 중심 서비스를 대표하는 커뮤니티시설이다. 아파트 단지 안의 공동 편의시설을 특화한 형태인 자이안센터는 입주민에게 고급스러운 삶의 방식을 실현할 수 있는 공간을 제공한다. 손님을 위한 게스트하우스는 물론이고, 주민들이 이용할 수 있는 독서실과 맞이 공간, 피트니스센터와 수영장, 클럽하우스 등 단지별 특성에 맞춰 다양한 편의시설이 조성돼 있다. 자이안센터가 입주민들의 생활을 한 단계 업그레이드하는 커뮤니티 시설의 대명사로 꼽히는 이유다.  \n" +
                "자이만의 독보적인 감성서비스도 주목할만 하다. GS건설은 지난 2004년부터 고객관계관리(CRM) 경영기법을 도입해, 입주 전 아파트 품질관리부터 입주 후 고객서비스까지 시스템을 연계한 밀착관리를 진행해왔다. 뿐만 아니라 고객서비스를 전문으로 하는 자회사 ‘자이S&D’와 협업해, 산하에 권역별 지역CS사무소 6곳을 운영 중이다.  \n" +
                "GS건설은 꾸준히 고객서비스조직을 정비하고 하자처리시스템을 체계화하는 등 눈에 보이지 않는 영역까지 고급화하기 위해 노력하고 있다. 일반적인 사후관리서비스센터에 고품격 휴게공간의 개념을 접목한 자이안라운지(Xian Lounge)가 그중 한 예다. 입주민들이 자이안라운지를 방문해 불편한 점에 대해 편안하게 상담할 수 있도록 배려했다. 분양에 초점을 맞춘 건설업계의 고객관리방식을 몇 단계 상승시킨 GS건설의 고객 감동 서비스에 해당한다.  \n" +
                "최첨단 기술을 다양하게 활용하는 점도 눈에 띈다. GS건설은 국내 건설사 중에서는 최초로 스마트폰 애플리케이션을 이용해 실시간 사후관리서비스 접수와 처리 업무를 진행한다. ‘고객안심 문자서비스’를 도입해 수리전문가가 자이 고객의 가정을 방문할 때 배정된 전문가의 사진과 수리 관련 정보 등을 미리 고객의 핸드폰으로 발송한다. 자이 소비자들이 안심하고 수리 담당자를 만날 수 있고, 사후관리과정을 믿고 맡길 수 있다는 점 때문에 크게 호평받고 있는 서비스다. GS건설 관계자는 “정보기술(IT)을 아파트 사후관리서비스에도 활용하는 이 같은 방침은 GS건설과 자이(Xi)의 ‘고객 중심 철학’에서 비롯된 세심한 고객서비스 정신을 반영한다”고 말했다. \n" +
                " \n" +
                "유한빛 기자 \n.";
        File font = new File(WORKING_PATH+"/chosunfont.ttf");
        GraphicsEnvironment ge =
                GraphicsEnvironment.getLocalGraphicsEnvironment();
        ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, font));

        System.out.println(Arrays.toString(ge.getAvailableFontFamilyNames()));
        InputStream targetStream = new FileInputStream(font);
        Font chosunFont = Font.createFont(Font.TRUETYPE_FONT, targetStream);
        File targetFile = new File(WORKING_PATH+"/out/"+"테스트.rtf");
        OutputStream outStream = new FileOutputStream(targetFile);
        RTFEditorKit RTF = new RTFEditorKit();


        Document doc = RTF.createDefaultDocument();
//        AttributeSet t = new SimpleAttributeSet();
//        AttributeSet.FontAttribute
//        Font font = new Font("조선",Font.BOLD,15);
//        t.
        doc.insertString(0,inputText,null);

//        RTF.write(outStream,doc,0,inputText.length());
//        RTF.read(new StringReader("테스트입니다."), doc, 0);
//        Str = doc.getText(0, doc.getLength());

        RTFEditorKit editorKit = new RTFEditorKit();
        StyledDocument document = (StyledDocument) editorKit.createDefaultDocument();
//        appendToStyledDocument(document);
        document.insertString(0,inputText,null);
        MutableAttributeSet fontFamily = new SimpleAttributeSet();
        StyleConstants.setFontFamily(fontFamily, "조선굴림체");
        document.setParagraphAttributes(0, document.getLength(), fontFamily, false);
        document.setCharacterAttributes(0, document.getLength(), fontFamily, false);
//        ByteArrayOutputStream os = new ByteArrayOutputStream();
        RTF.write(outStream,document,0,inputText.length());
    }

}
