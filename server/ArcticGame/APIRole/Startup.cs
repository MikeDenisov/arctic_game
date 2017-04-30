using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Web.Http;
using API.Config;
using Owin;
using SelfHost;

namespace APIRole
{
    class Startup
    {
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
