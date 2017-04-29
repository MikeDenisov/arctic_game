using System.Web.Http;
using API.Config;
using Owin;

namespace SelfHost
{
    public class Startup
    {
        // This code configures Web API. The Startup class is specified as a type
        // parameter in the WebApp.Start method.
        public void Configuration(IAppBuilder appBuilder)
        {
            // Configure Web API for self-host. 
            HttpConfiguration config = new HttpConfiguration();

            AutofacConfig.Initialize(config);
            RouteConfig.RegisterRoutes(config);
            ApiConfig.Setup(config);
            EFConfig.Initialize();

            appBuilder.UseWebApi(config);
        }
    }
}
