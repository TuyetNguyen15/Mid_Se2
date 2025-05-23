import React, { useEffect, useState } from "react";
import { fetchRooms } from "../services/RoomService";
import Header from "../components/header";
import { getRoomThumbnail } from "../utils/ImageUtils";
import '../components/styles.css'; 
import Footer from "../components/footer";
import { Link } from "react-router-dom";
import SearchBox from "../components/SearchBox";

const Home = () => {
  const [rooms, setRooms] = useState([]);

  useEffect(() => {
    fetchRooms()
      .then((res) => {
        const fixedFive = res.data.slice(0, 5); 
        setRooms(fixedFive);
      })
      .catch((err) => console.error("Load error:", err));
  }, []);

  return (
    <div className="fade-page">
      <Header />
      <SearchBox redirect={true}/>
      <div className="container">
        {/* ABOUT US */}
        <div className="about-section">
          <div className="row">
            <div className="col-md-6 col-12 d-flex align-items-center">
              <div className="about-content">
                <h2>ABOUT US</h2>
                <p className="lead font-weight-bold">Welcome to Mercy Hotel!</p>
                <p>
                  At Mercy Hotel, we are committed to providing you with an exceptional stay.
                  With luxurious and modern amenities, our hotel is the ideal destination
                  for both leisure and business travelers. We take pride in our professional
                  staff, who are always ready to serve and meet all your needs.
                </p>
                <Link to="/browseRooms" className="btn-find-out mt-3">Find Out More</Link>
              </div>
            </div>
            <div className="col-md-6 col-12">
              <div className="about-images">
                <img src="/assets/images/rooms/01.jpg" className="img-fluid img-back" alt="Hotel" />
                <img src="/assets/images/rooms/02.jpg" className="img-fluid img-front" alt="Hotel" />
              </div>
            </div>
          </div>
        </div>

        {/* OUR CHOICE */}
        <div className="box2">
          <div className="row">
            <div className="col-12 text-center mb-4">
              <h2>OUR CHOICE</h2>
              <p className="lead font-weight-bold">The best room just for you!</p>
            </div>
          </div>
          <div className="row card-container g-4">
            {rooms[1] && (
              <div className="col-md-4 my-4" style={{ height: '550px' }}>
                <Link to={`/rooms/${rooms[1].id}`}>
                  <div className="card room-card">
                    <img src={getRoomThumbnail(rooms[1])} alt="Room" className="room-img" />
                    <div className="card-overlay">
                      <h5>{rooms[1].roomType}</h5>
                      <p>${rooms[1].roomPrice}/night</p>
                    </div>
                  </div>
                </Link>
              </div>
            )}
            {rooms[2] && (
              <div className="col-md-8 my-4" style={{ height: '550px' }}>
                <Link to={`/rooms/${rooms[2].id}`}>
                  <div className="card room-card">
                    <img src={getRoomThumbnail(rooms[1])} alt="Room" className="room-img" />
                    <div className="card-overlay">
                      <h5>{rooms[2].roomType}</h5>
                      <p>${rooms[2].roomPrice}/night</p>
                    </div>
                  </div>
                </Link>
              </div>
            )}
            {rooms.slice(0, 3).map((room) => (
              <div key={room.id} className="col-md-4 my-4" style={{ height: '400px' }}>
                <Link to={`/rooms/${room.id}`}>
                  <div className="card room-card">
                    <img src={getRoomThumbnail(room)} alt="Room" className="room-img" />
                    <div className="card-overlay">
                      <h5>{room.roomType}</h5>
                      <p>${room.roomPrice}/night</p>
                    </div>
                  </div>
                </Link>
              </div>
            ))}
          </div>
        </div>
      </div>
      <Footer />
    </div>
  );
};

export default Home;
