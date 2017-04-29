using System;
using System.Data.Entity;
using System.Data.Entity.Infrastructure;
using System.Linq;
using System.Linq.Expressions;

namespace Domain.Core
{
    public class EntityRepository<T, TKey> : IEntityRepository<T, TKey>
        where T : class, IEntity<TKey>, new()
        where TKey : IComparable
    {
        readonly DbContext _entitiesContext;

        public EntityRepository(DbContext entitiesContext)
        {
            if (entitiesContext == null)
            {

                throw new ArgumentNullException("entitiesContext");
            }

            _entitiesContext = entitiesContext;
        }

        public virtual IQueryable<T> GetAll()
        {
            return _entitiesContext.Set<T>();
        }

        public virtual IQueryable<T> AllIncluding(
            params Expression<Func<T, object>>[] includeProperties)
        {
            IQueryable<T> query = _entitiesContext.Set<T>();
            foreach (var includeProperty in includeProperties)
            {
                query = query.Include(includeProperty);
            }

            return query;
        }

        public T GetSingle(TKey key)
        {
            return GetAll().FirstOrDefault(x => x.Key.Equals(key));
        }

        public virtual IQueryable<T> FindBy(Expression<Func<T, bool>> predicate)
        {
            return _entitiesContext.Set<T>().Where(predicate);
        }

        public virtual void Add(T entity)
        {
            DbEntityEntry dbEntityEntry = _entitiesContext.Entry<T>(entity);
            _entitiesContext.Set<T>().Add(entity);
        }

        public virtual void Edit(T entity)
        {
            DbEntityEntry dbEntityEntry = _entitiesContext.Entry<T>(entity);
            dbEntityEntry.State = EntityState.Modified;
        }

        public virtual void Delete(T entity)
        {
            DbEntityEntry dbEntityEntry = _entitiesContext.Entry<T>(entity);
            dbEntityEntry.State = EntityState.Deleted;
        }

        public virtual void DeleteGraph(T entity)
        {
            DbSet<T> dbSet = _entitiesContext.Set<T>();
            dbSet.Attach(entity);
            dbSet.Remove(entity);
        }

        public virtual void Save()
        {
            _entitiesContext.SaveChanges();
        }
    }
}
