public class Seasons {

    // method season() receives integer and returns season name. Integer must be in [1,12]. If not exception will be thrown
    public String season(int monthNumber){
        if(monthNumber < 1 || monthNumber > 12) {
            throw new Error("Month number must be in [1,12], your entered: " + monthNumber);
        }
        switch (monthNumber){
            case 1:
            case 2:
            case 12:
                return "Winter";
            case 3:
            case 4:
            case 5:
                return "Spring";
            case 6:
            case 7:
            case 8:
                return "Summer";
            case 9:
            case 10:
            case 11:
                return "Autumn";
            default:
                throw new Error("Can not identify season by month: " + monthNumber);
        }
    }

}
