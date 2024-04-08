
package com.crio.warmup.stock;


import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.crio.warmup.stock.portfolio.PortfolioManagerImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;


public class PortfolioManagerApplication {

  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
    ObjectMapper ob = getObjectMapper();
    PortfolioTrade t[] = ob.readValue(resolveFileFromResources(args[0]), PortfolioTrade[].class);

    List<String> symbols = new ArrayList<>();

    for(int i = 0; i < t.length; i++){
      symbols.add(t[i].getSymbol());
      System.out.println(t[i].getSymbol());
    }
    
     return symbols;
  }




  // // TODO: CRIO_TASK_MODULE_CALCULATIONS
  // //  Now that you have the list of PortfolioTrade and their data, calculate annualized returns
  // //  for the stocks provided in the Json.
  // //  Use the function you just wrote #calculateAnnualizedReturns.
  // //  Return the list of AnnualizedReturns sorted by annualizedReturns in descending order.



  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(
        Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    return objectMapper;
  }


  // TODO: CRIO_TASK_MODULE_JSON_PARSING
  //  Follow the instructions provided in the task documentation and fill up the correct values for
  //  the variables provided. First value is provided for your reference.
  //  A. Put a breakpoint on the first line inside mainReadFile() which says
  //    return Collections.emptyList();
  //  B. Then Debug the test #mainReadFile provided in PortfoliomanagerApplicationTest.java
  //  following the instructions to run the test.
  //  Once you are able to run the test, perform following tasks and record the output as a
  //  String in the function below.
  //  Use this link to see how to evaluate expressions -
  //  https://code.visualstudio.com/docs/editor/debugging#_data-inspection
  //  1. evaluate the value of "args[0]" and set the value
  //     to the variable named valueOfArgument0 (This is implemented for your reference.)
  //  2. In the same window, evaluate the value of expression below and set it
  //  to resultOfResolveFilePathArgs0
  //     expression ==> resolveFileFromResources(args[0])
  //  3. In the same window, evaluate the value of expression below and set it
  //  to toStringOfObjectMapper.
  //  You might see some garbage numbers in the output. Dont worry, its expected.
  //    expression ==> getObjectMapper().toString()
  //  4. Now Go to the debug window and open stack trace. Put the name of the function you see at
  //  second place from top to variable functionNameFromTestFileInStackTrace
  //  5. In the same window, you will see the line number of the function in the stack trace window.
  //  assign the same to lineNumberFromTestFileInStackTrace
  //  Once you are done with above, just run the corresponding test and
  //  make sure its working as expected. use below command to do the same.
  //  ./gradlew test --tests PortfolioManagerApplicationTest.testDebugValues
  public static List<String> debugOutputs() {

     String valueOfArgument0 = "trades.json";
     String resultOfResolveFilePathArgs0 = "/home/crio-user/workspace/afan-jawaid007-ME_QMONEY_V2/qmoney/bin/main/trades.json";
     String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper";
     String functionNameFromTestFileInStackTrace = "PortfolioManagerApplicationTest.mainReadFile()";
     String lineNumberFromTestFileInStackTrace = "22";


    return Arrays.asList(new String[]{valueOfArgument0, resultOfResolveFilePathArgs0,
        toStringOfObjectMapper, functionNameFromTestFileInStackTrace,
        lineNumberFromTestFileInStackTrace});
  }


  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.
  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
      List<PortfolioTrade> trade = readTradesFromJson(args[0]);
       RestTemplate rest = new RestTemplate();
      TotalReturnsDto returns[] = new TotalReturnsDto[trade.size()];
      LocalDate endDate = LocalDate.parse(args[1]);

      for(int i = 0; i < trade.size(); i++){
        TiingoCandle can[] = rest.getForObject(prepareUrl(trade.get(i), endDate, getToken()), TiingoCandle[].class);
        returns[i] = new TotalReturnsDto(trade.get(i).getSymbol(), can[can.length-1].getClose());
      }
      
      Arrays.sort(returns, (a, b) -> (int)(a.getClosingPrice()-b.getClosingPrice()));

      List<String> ans = new ArrayList<>();

      for(int i = 0; i < returns.length; i++){
        ans.add(returns[i].getSymbol());
      }

      return ans;
  }

  // TODO:
  //  After refactor, make sure that the tests pass by using these two commands
  //  ./gradlew test --tests PortfolioManagerApplicationTest.readTradesFromJson
  //  ./gradlew test --tests PortfolioManagerApplicationTest.mainReadFile
  public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {
      ObjectMapper ob = getObjectMapper();
      PortfolioTrade t[] = ob.readValue(resolveFileFromResources(filename), PortfolioTrade[].class);
      List<PortfolioTrade> ans = new ArrayList<>();

      for(int i = 0; i < t.length; i++){
        ans.add(t[i]);
      }

     return ans;
  }


  // TODO:
  //  Build the Url using given parameters and use this function in your code to cann the API.
  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
     return "https://api.tiingo.com/tiingo/daily/"+trade.getSymbol()+"/prices?startDate="+trade.getPurchaseDate()+"&endDate="+endDate
     +"&token="+token;
  }


  // TODO:
  //  Ensure all tests are passing using below command
  //  ./gradlew test --tests ModuleThreeRefactorTest
  static Double getOpeningPriceOnStartDate(List<Candle> candles) {
     return candles.get(0).getOpen();
  }


  public static Double getClosingPriceOnEndDate(List<Candle> candles) {
     return candles.get(candles.size()-1).getClose();
  }


  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token) {
    RestTemplate rest = new RestTemplate();
    List<Candle> candles = new ArrayList<>();
    TiingoCandle ti[] = rest.getForObject(prepareUrl(trade, endDate, token), TiingoCandle[].class);

    for(TiingoCandle cpy : ti){
      candles.add(cpy);
    }
    
     return candles;
  }

  public static String getToken(){
    return "f11a0bb01c0a9a5c6d63ff5721f1a2faeaaa5a0f";
  }

  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args)
      throws IOException, URISyntaxException {
     
        List<PortfolioTrade> trade = readTradesFromJson(args[0]);
        LocalDate endDate = LocalDate.parse(args[1]);

        List<AnnualizedReturn> annual = new ArrayList<>();

        for(int i = 0; i < trade.size(); i++){
            List<Candle> candles = fetchCandles(trade.get(i), endDate, getToken());
            annual.add(calculateAnnualizedReturns(endDate, trade.get(i), getOpeningPriceOnStartDate(candles), getClosingPriceOnEndDate(candles)));
        }

        Collections.sort(annual, (a, b) -> b.getAnnualizedReturn().compareTo(a.getAnnualizedReturn()));

        return annual;
  }

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  //  Return the populated list of AnnualizedReturn for all stocks.
  //  Annualized returns should be calculated in two steps:
  //   1. Calculate totalReturn = (sell_value - buy_value) / buy_value.
  //      1.1 Store the same as totalReturns
  //   2. Calculate extrapolated annualized returns by scaling the same in years span.
  //      The formula is:
  //      annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
  //      2.1 Store the same as annualized_returns
  //  Test the same using below specified command. The build should be successful.
  //     ./gradlew test --tests PortfolioManagerApplicationTest.testCalculateAnnualizedReturn

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate,
      PortfolioTrade trade, Double buyPrice, Double sellPrice) {
      
        long days = ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate);
        double totalReturn = (sellPrice - buyPrice)/buyPrice;
        double time = ((double)days)/365;
        double annualized = Math.pow((1+totalReturn), (1/time))-1;

        return new AnnualizedReturn(trade.getSymbol(), annualized, totalReturn);
  }























  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Once you are done with the implementation inside PortfolioManagerImpl and
  //  PortfolioManagerFactory, create PortfolioManager using PortfolioManagerFactory.
  //  Refer to the code from previous modules to get the List<PortfolioTrades> and endDate, and
  //  call the newly implemented method in PortfolioManager to calculate the annualized returns.

  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.

  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {

       ObjectMapper om = new ObjectMapper();
       String file = args[0];
       LocalDate endDate = LocalDate.parse(args[1]);
       PortfolioManagerApplication portfolioManager = new PortfolioManagerApplication();
       PortfolioTrade portfolioTrades[] = om.readValue(file, PortfolioTrade[].class);
      //  String contents = readFileAsString(file);
      //  ObjectMapper objectMapper = getObjectMapper();
       return portfolioManager.calculateAnnualizedReturn(Arrays.asList(portfolioTrades), endDate);
  }


  private List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> asList,
      LocalDate endDate) {
    return null;
  }





















  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());




    printJsonObject(mainCalculateReturnsAfterRefactor(args));
  }
}

