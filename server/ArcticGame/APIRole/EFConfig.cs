using System;
using System.Collections.Generic;
using System.Data.Entity.Migrations;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace SelfHost
{
    public class EFConfig
    {
        public static void Initialize()
        {
            RunMigrations();
        }

        private static void RunMigrations()
        {
            var efMigrationSettings = new Domain.Migrations.Configuration();
            var efMigrator = new DbMigrator(efMigrationSettings);

            efMigrator.Update();
        }
    }
}
