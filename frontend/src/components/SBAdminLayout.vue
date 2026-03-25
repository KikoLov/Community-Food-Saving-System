<template>
  <div class="sb-nav-fixed">
    <!-- Top Navbar -->
    <nav class="sb-topnav navbar navbar-expand navbar-dark bg-dark">
      <!-- Navbar Brand-->
      <a class="navbar-brand ps-3" href="#">{{ brandName }}</a>
      <!-- Sidebar Toggle-->
      <button class="btn btn-link btn-sm order-1 order-lg-0 me-4 me-lg-0" id="sidebarToggle" href="#!">
        <i class="fas fa-bars"></i>
      </button>
      <!-- Navbar Search-->
      <form class="d-none d-md-inline-block form-inline ms-auto me-0 me-md-3 my-2 my-md-0">
        <div class="input-group">
          <input class="form-control" type="text" placeholder="搜索..." aria-label="Search for..." aria-describedby="btnNavbarSearch" />
          <button class="btn btn-primary" id="btnNavbarSearch" type="button">
            <i class="fas fa-search"></i>
          </button>
        </div>
      </form>
      <!-- Navbar-->
      <ul class="navbar-nav ms-auto ms-md-0 me-3 me-lg-4">
        <li class="nav-item dropdown">
          <a class="nav-link dropdown-toggle" id="navbarDropdown" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false">
            <i class="fas fa-user fa-fw"></i>
            <span class="ms-2 d-none d-lg-inline">{{ userName }}</span>
          </a>
          <ul class="dropdown-menu dropdown-menu-end" aria-labelledby="navbarDropdown">
            <slot name="user-menu"></slot>
            <li><hr class="dropdown-divider" /></li>
            <li>
              <a class="dropdown-item" href="#" @click.prevent="handleLogout">
                <i class="fas fa-sign-out-alt fa-sm fa-fw me-2 text-gray-400"></i>
                退出登录
              </a>
            </li>
          </ul>
        </li>
      </ul>
    </nav>

    <!-- Layout Sidenav -->
    <div id="layoutSidenav">
      <!-- Sidebar Navigation -->
      <div id="layoutSidenav_nav">
        <nav class="sb-sidenav accordion sb-sidenav-dark" id="sidenavAccordion">
          <div class="sb-sidenav-menu">
            <div class="nav">
              <slot name="menu"></slot>
            </div>
          </div>
          <div class="sb-sidenav-footer">
            <div class="small">登录用户:</div>
            {{ userName }}
          </div>
        </nav>
      </div>

      <!-- Main Content -->
      <div id="layoutSidenav_content">
        <main>
          <div class="container-fluid px-4">
            <div v-if="pageTitle" class="d-sm-flex align-items-center justify-content-between mb-4">
              <h1 class="h3 mb-0">{{ pageTitle }}</h1>
            </div>
            <slot></slot>
          </div>
          <footer class="py-4 bg-light mt-auto">
            <div class="container-fluid px-4">
              <div class="d-flex align-items-center justify-content-between small">
                <div class="text-muted">Copyright &copy; 食品临期平台 2024</div>
                <div>
                  <a href="#" class="text-muted me-2">隐私政策</a>
                  &middot;
                  <a href="#" class="text-muted ms-2">使用条款</a>
                </div>
              </div>
            </div>
          </footer>
        </main>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { initSidebarToggle } from '@/utils/sb-admin'

const props = defineProps({
  pageTitle: {
    type: String,
    default: ''
  },
  brandName: {
    type: String,
    default: '食品临期平台'
  }
})

const router = useRouter()
const userStore = useUserStore()

const userName = computed(() => {
  return userStore.userInfo?.nickName || userStore.userInfo?.userName || '用户'
})

const handleLogout = () => {
  userStore.logoutAction()
  router.push('/login')
}

onMounted(() => {
  initSidebarToggle()
})

onUnmounted(() => {
  // Cleanup if needed
})
</script>

<style>
/* Override SB Admin styles to match Chinese theme */
.sb-topnav {
  height: 56px;
  z-index: 1039;
}

.sb-sidenav {
  display: flex;
  flex-direction: column;
  height: 100%;
  flex-wrap: nowrap;
}

.sb-sidenav .sb-sidenav-menu {
  flex-grow: 1;
}

.sb-sidenav .nav {
  flex-direction: column;
  flex-wrap: nowrap;
}

.sb-sidenav .nav .sb-sidenav-menu-heading {
  padding: 1.75rem 1rem 0.75rem;
  font-size: 0.75rem;
  font-weight: bold;
  text-transform: uppercase;
  letter-spacing: 0.1em;
}

.sb-sidenav .nav .nav-link {
  display: flex;
  align-items: center;
  padding: 1rem 1rem;
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  transition: all 0.2s ease-in-out;
}

.sb-sidenav .nav .nav-link .sb-nav-link-icon {
  font-size: 0.9rem;
  margin-right: 0.5rem;
}

.sb-sidenav .nav .nav-link:hover {
  color: #fff;
  background-color: rgba(255, 255, 255, 0.1);
}

.sb-sidenav .nav .nav-link.active {
  color: #fff;
  font-weight: 700;
  background-color: rgba(255, 255, 255, 0.15);
}

.sb-sidenav .sb-sidenav-footer {
  padding: 1.75rem 1rem;
  border-top: 1px solid rgba(255, 255, 255, 0.15);
}

.sb-sidenav-dark {
  background-color: #212529;
  background-image: linear-gradient(180deg, #212529 10%, #212529 100%);
  background-size: cover;
}

.sb-sidenav-dark .sb-sidenav-menu-heading {
  color: rgba(255, 255, 255, 0.4);
}

.sb-sidenav-dark .nav-link {
  color: rgba(255, 255, 255, 0.8);
}

.sb-sidenav-dark .nav-link:hover {
  color: #fff;
  background-color: rgba(255, 255, 255, 0.1);
}

.sb-sidenav-dark .nav-link.active {
  color: #fff;
  background-color: rgba(255, 255, 255, 0.15);
}

.sb-sidenav-dark .sb-sidenav-footer {
  background-color: rgba(0, 0, 0, 0.3);
  color: rgba(255, 255, 255, 0.8);
}

#layoutSidenav {
  display: flex;
}

#layoutSidenav #layoutSidenav_nav {
  flex-basis: 225px;
  flex-shrink: 0;
  transition: transform 0.15s ease-in-out;
  z-index: 1038;
}

#layoutSidenav #layoutSidenav_content {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  min-width: 0;
  flex-grow: 1;
  min-height: calc(100vh - 56px);
}

@media (min-width: 992px) {
  #layoutSidenav #layoutSidenav_nav {
    transform: translateX(0);
  }
}

.sb-sidenav-toggled #layoutSidenav #layoutSidenav_nav {
  transform: translateX(-225px);
}

@media (max-width: 991.98px) {
  #layoutSidenav #layoutSidenav_nav {
    transform: translateX(-225px);
  }

  #layoutSidenav.sb-sidenav-toggled #layoutSidenav_nav {
    transform: translateX(0);
  }
}

/* Collapse arrow */
.sb-sidenav-collapse-arrow {
  display: inline-block;
  margin-left: auto;
  transition: transform 0.15s ease-in-out;
}

.nav-link.collapsed .sb-sidenav-collapse-arrow {
  transform: rotate(-90deg);
}

/* Custom scrollbar for sidebar */
.sb-sidenav {
  scrollbar-width: thin;
  scrollbar-color: rgba(255, 255, 255, 0.3) transparent;
}

.sb-sidenav::-webkit-scrollbar {
  width: 6px;
}

.sb-sidenav::-webkit-scrollbar-track {
  background: transparent;
}

.sb-sidenav::-webkit-scrollbar-thumb {
  background-color: rgba(255, 255, 255, 0.3);
  border-radius: 3px;
}

.sb-sidenav::-webkit-scrollbar-thumb:hover {
  background-color: rgba(255, 255, 255, 0.5);
}
</style>
