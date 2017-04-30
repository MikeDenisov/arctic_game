using System.Net.Http.Headers;
using System.Web.Http;

namespace API.Config
{
    public class ApiConfig
    {
        public static void Setup(HttpConfiguration config)
        {
            config.Formatters.JsonFormatter.SupportedMediaTypes
                .Add(new MediaTypeHeaderValue("text/html"));
        }
    }
}