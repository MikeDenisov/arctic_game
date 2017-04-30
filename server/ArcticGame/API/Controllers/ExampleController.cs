using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Http;
using System.Web.Http;

namespace API.Controllers
{  
    public class ExampleController : ApiController
    {
        [Route("~/api/yo")]
        [HttpGet]
        public string Test()
        {
            return "yo";
        }
    }
}
