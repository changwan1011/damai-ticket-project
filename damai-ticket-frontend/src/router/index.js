import { createRouter, createWebHistory } from "vue-router";

import Login from "../views/Login.vue";
import EventList from "../views/EventList.vue";
import SeatSelect from "../views/SeatSelect.vue";
import MyOrders from "../views/MyOrders.vue";
import Profile from "../views/Profile.vue";
import Admin from "../views/Admin.vue";
import Favorites from "../views/Favorites.vue";
import ETicket from "../views/ETicket.vue";

const router = createRouter({
    history: createWebHistory(),
    routes: [
        { path: "/", redirect: "/events" },
        { path: "/login", name: "login", component: Login },
        { path: "/events", name: "events", component: EventList },
        { path: "/seat/:showId", name: "seat", component: SeatSelect },
        { path: "/orders", name: "orders", component: MyOrders },
        { path: "/profile", name: "profile", component: Profile },
        { path: "/admin", name: "admin", component: Admin },
        { path: "/favorites", name: "favorites", component: Favorites },
        { path: "/eticket", name: "eticket", component: ETicket },
        { path: "/:pathMatch(.*)*", redirect: "/events" },
    ],
});

export default router;
