package ads.check.rate.exchange_rate_check.api;


import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface CryptonatorApi {

    String API_BASE_URL = "https://api.cryptonator.com/api/ticker/";

    @GET("{param}")
    Call<CryptonatorResponse> tick(@Path("param") String param);
}
