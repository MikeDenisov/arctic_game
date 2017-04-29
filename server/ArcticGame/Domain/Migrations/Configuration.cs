using System;
using System.Data.Entity.Migrations;
using Domain.Core;
using Domain.Entities;

namespace Domain.Migrations
{
    public class Configuration : DbMigrationsConfiguration<EntitiesContext>
    {
        public Configuration()
        {
            AutomaticMigrationsEnabled = true;
        }

        protected override void Seed(EntitiesContext context)
        {
            context.Users.AddOrUpdate(new User()
            {
                Key = 1,
                Name = "FirstNah"
            });

            context.Photos.AddOrUpdate(new Photo()
            {
                UserKey = 1,
                Key = 1,
                Comment = "chipmunk",
                LocalPath = "0.jpg",
                TimeStamp = DateTime.Now,
                Location = new Location()
                {
                    Azimuth = 0,
                    Latitude = 0,
                    Longtitude = 0.1
                }
            });

            base.Seed(context);
        }
    }
}
